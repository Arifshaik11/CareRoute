package com.innovation_matrix.careroute.demo;

import java.time.Clock;
import java.time.Duration;

import com.innovation_matrix.careroute.followup.FollowUpRepository;
import com.innovation_matrix.careroute.followup.FollowUpStatus;
import com.innovation_matrix.careroute.patient.PatientRepository;
import com.innovation_matrix.careroute.referral.ReferralRepository;
import com.innovation_matrix.careroute.referral.ReferralService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Demo controls: simulate time passing and restore the synthetic dataset. */
@Service
public class DemoService {

    private final EntityManager entityManager;
    private final PatientRepository patients;
    private final ReferralRepository referrals;
    private final FollowUpRepository followUps;
    private final ReferralService referralService;
    private final DemoDataSeeder seeder;
    private final Clock clock;

    public DemoService(EntityManager entityManager, PatientRepository patients, ReferralRepository referrals,
                       FollowUpRepository followUps, ReferralService referralService, DemoDataSeeder seeder,
                       Clock clock) {
        this.entityManager = entityManager;
        this.patients = patients;
        this.referrals = referrals;
        this.followUps = followUps;
        this.referralService = referralService;
        this.seeder = seeder;
        this.clock = clock;
    }

    @Transactional
    public void seedIfEmpty() {
        if (patients.count() == 0) {
            seeder.seed();
        }
    }

    /**
     * "Fast-forward" open work by pulling every open deadline earlier, then running escalation.
     * Equivalent to waiting {@code hours} hours, without touching the system clock.
     */
    @Transactional
    public FastForwardResult fastForward(int hours) {
        Duration amount = Duration.ofHours(hours);
        var openReferrals = referrals.findByStatusIn(ReferralService.OPEN_STATUSES);
        openReferrals.forEach(r -> r.shiftDueAt(amount));
        var pending = followUps.findByStatus(FollowUpStatus.PENDING);
        pending.forEach(f -> f.shiftDueAt(amount));
        entityManager.flush();
        int escalated = referralService.escalateOverdue(clock.instant());
        return new FastForwardResult(hours, openReferrals.size(), pending.size(), escalated);
    }

    /** Wipes patient data (rules and users are kept) and re-seeds the synthetic block. */
    @Transactional
    public void reset() {
        entityManager.flush();
        entityManager.createNativeQuery(
                        "truncate table referral_event, follow_up, referral, visit, patient, audit_log restart identity")
                .executeUpdate();
        entityManager.clear();
        seeder.seed();
    }

    public record FastForwardResult(int hours, int referralsShifted, int followUpsShifted, int escalated) {
    }
}
