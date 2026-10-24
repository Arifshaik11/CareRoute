package com.innovation_matrix.careroute.dashboard;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.innovation_matrix.careroute.followup.FollowUp;
import com.innovation_matrix.careroute.followup.FollowUpRepository;
import com.innovation_matrix.careroute.followup.FollowUpStatus;
import com.innovation_matrix.careroute.location.Village;
import com.innovation_matrix.careroute.location.VillageRepository;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.patient.PatientRepository;
import com.innovation_matrix.careroute.patient.PatientSpecifications;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.referral.ReferralRepository;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.AppUserRepository;
import com.innovation_matrix.careroute.user.Role;
import com.innovation_matrix.careroute.visit.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates for the supervisor dashboard. Computed in memory, which is fine at
 * block scale (thousands of rows); at district scale this would move to SQL views.
 */
@Service
public class DashboardService {

    private static final int MAX_ESCALATIONS_SHOWN = 10;

    private final PatientRepository patients;
    private final VisitRepository visits;
    private final FollowUpRepository followUps;
    private final ReferralRepository referrals;
    private final VillageRepository villages;
    private final AppUserRepository users;
    private final Clock clock;

    public DashboardService(PatientRepository patients, VisitRepository visits, FollowUpRepository followUps,
                            ReferralRepository referrals, VillageRepository villages, AppUserRepository users,
                            Clock clock) {
        this.patients = patients;
        this.visits = visits;
        this.followUps = followUps;
        this.referrals = referrals;
        this.villages = villages;
        this.users = users;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardDto build() {
        Instant now = clock.instant();
        List<Patient> allPatients = patients.findAll(PatientSpecifications.matching(null, null, null, null));
        List<FollowUp> pending = followUps.findByStatusWithVillage(FollowUpStatus.PENDING);
        List<Referral> allReferrals = referrals.findAllForList();

        Predicate<Referral> open = r -> r.getStatus().isOpen();
        Predicate<Referral> escalated = r -> open.test(r) && r.getEscalatedAt() != null;

        var kpis = new DashboardDto.Kpis(
                allPatients.size(),
                visits.countByVisitedAtAfter(now.minus(Duration.ofDays(7))),
                pending.size(),
                pending.stream().filter(f -> f.isOverdue(now)).count(),
                allReferrals.stream().filter(open).count(),
                allReferrals.stream().filter(escalated).count(),
                completionRate(allReferrals));

        var funnel = new DashboardDto.Funnel(
                allReferrals.size(),
                allReferrals.stream().filter(r -> r.getAcknowledgedAt() != null).count(),
                allReferrals.stream().filter(r -> r.getArrivedAt() != null).count(),
                allReferrals.stream().filter(r -> r.getClosedAt() != null).count(),
                allReferrals.stream().filter(r -> r.isOverdue(now)).count());

        Map<Long, String> ashaByVillage = users.findByRoleAndActiveTrue(Role.ASHA).stream()
                .filter(u -> u.getVillage() != null)
                .collect(Collectors.toMap(u -> u.getVillage().getId(), AppUser::getFullName, (a, b) -> a + ", " + b));
        Map<Long, List<Patient>> patientsByVillage = groupBy(allPatients, p -> p.getVillage().getId());
        Map<Long, List<FollowUp>> pendingByVillage = groupBy(pending, f -> f.getPatient().getVillage().getId());
        Map<Long, List<Referral>> referralsByVillage = groupBy(allReferrals, r -> r.getPatient().getVillage().getId());

        List<DashboardDto.VillageRow> rows = villages.findAllByOrderByNameAsc().stream()
                .map(v -> villageRow(v, now, ashaByVillage,
                        patientsByVillage.getOrDefault(v.getId(), List.of()),
                        pendingByVillage.getOrDefault(v.getId(), List.of()),
                        referralsByVillage.getOrDefault(v.getId(), List.of()),
                        open, escalated))
                .toList();

        Map<Program, Long> byProgram = new EnumMap<>(Program.class);
        for (Program program : Program.values()) {
            byProgram.put(program, allPatients.stream().filter(p -> p.getProgram() == program).count());
        }

        List<ReferralDto> activeEscalations = allReferrals.stream()
                .filter(escalated)
                .sorted(Comparator.comparing(Referral::getEscalatedAt).reversed())
                .limit(MAX_ESCALATIONS_SHOWN)
                .map(r -> ReferralDto.summary(r, now))
                .toList();

        return new DashboardDto(now, kpis, funnel, rows, byProgram, activeEscalations);
    }

    private static DashboardDto.VillageRow villageRow(Village v, Instant now, Map<Long, String> ashaByVillage,
                                                      List<Patient> villagePatients, List<FollowUp> villagePending,
                                                      List<Referral> villageReferrals,
                                                      Predicate<Referral> open, Predicate<Referral> escalated) {
        return new DashboardDto.VillageRow(
                v.getId(),
                v.getName(),
                ashaByVillage.getOrDefault(v.getId(), "Unassigned"),
                villagePatients.size(),
                villagePending.size(),
                villagePending.stream().filter(f -> f.isOverdue(now)).count(),
                villageReferrals.stream().filter(open).count(),
                villageReferrals.stream().filter(escalated).count(),
                completionRate(villageReferrals));
    }

    /** Share of referrals where the patient actually reached the facility; null when there are none. */
    private static Double completionRate(Collection<Referral> referrals) {
        if (referrals.isEmpty()) {
            return null;
        }
        long arrived = referrals.stream().filter(r -> r.getArrivedAt() != null).count();
        return Math.round(1000.0 * arrived / referrals.size()) / 1000.0;
    }

    private static <T> Map<Long, List<T>> groupBy(List<T> items, Function<T, Long> key) {
        return items.stream().collect(Collectors.groupingBy(key));
    }
}
