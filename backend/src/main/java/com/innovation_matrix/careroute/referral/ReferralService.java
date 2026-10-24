package com.innovation_matrix.careroute.referral;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.innovation_matrix.careroute.audit.AuditService;
import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.common.error.NotFoundException;
import com.innovation_matrix.careroute.common.error.ValidationException;
import com.innovation_matrix.careroute.followup.FollowUp;
import com.innovation_matrix.careroute.followup.FollowUpRepository;
import com.innovation_matrix.careroute.followup.FollowUpStatus;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;
import com.innovation_matrix.careroute.security.AccessPolicy;
import com.innovation_matrix.careroute.security.AuthUser;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferralService {

    public static final Set<ReferralStatus> OPEN_STATUSES = EnumSet.of(ReferralStatus.REFERRED, ReferralStatus.ACKNOWLEDGED);

    /** How long the ASHA has to act once a referral escalates back to her. */
    static final Duration ESCALATION_FOLLOW_UP_WINDOW = Duration.ofHours(24);

    private static final Logger log = LoggerFactory.getLogger(ReferralService.class);

    private final ReferralRepository referrals;
    private final FollowUpRepository followUps;
    private final AppUserRepository users;
    private final AccessPolicy access;
    private final AuditService audit;
    private final Clock clock;

    public ReferralService(ReferralRepository referrals, FollowUpRepository followUps, AppUserRepository users,
                           AccessPolicy access, AuditService audit, Clock clock) {
        this.referrals = referrals;
        this.followUps = followUps;
        this.users = users;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReferralDto> list(AuthUser user, ReferralStatus status, boolean escalatedOnly) {
        Instant now = clock.instant();
        List<Referral> scoped = switch (user.getRole()) {
            case ASHA -> referrals.findForAsha(user.getId());
            case PHC_STAFF -> referrals.findForFacility(user.getFacilityId());
            case SUPERVISOR -> referrals.findAllForList();
        };
        return scoped.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> !escalatedOnly || (r.getEscalatedAt() != null && r.getStatus().isOpen()))
                .map(r -> ReferralDto.summary(r, now))
                .toList();
    }

    @Transactional
    public ReferralDto get(AuthUser user, Long id) {
        Referral referral = find(id);
        access.checkCanView(user, referral);
        audit.record(user.getId(), "REFERRAL_VIEWED", "PATIENT", referral.getPatient().getId(), "Referral #" + id);
        return ReferralDto.detail(referral, clock.instant());
    }

    @Transactional
    public ReferralDto acknowledge(AuthUser user, Long id, String note) {
        Referral referral = find(id);
        access.checkCanUpdate(user, referral);
        referral.acknowledge(actor(user), blankToNull(note), clock.instant());
        audit.record(user.getId(), "REFERRAL_ACKNOWLEDGED", "PATIENT", referral.getPatient().getId(), "Referral #" + id);
        return ReferralDto.detail(referral, clock.instant());
    }

    @Transactional
    public ReferralDto markArrived(AuthUser user, Long id, String note) {
        Referral referral = find(id);
        access.checkCanUpdate(user, referral);
        Instant now = clock.instant();
        referral.markArrived(actor(user), blankToNull(note), now);
        resolveEscalationTasks(referral, now);
        audit.record(user.getId(), "REFERRAL_ARRIVED", "PATIENT", referral.getPatient().getId(), "Referral #" + id);
        return ReferralDto.detail(referral, now);
    }

    @Transactional
    public ReferralDto close(AuthUser user, Long id, String outcomeNote) {
        if (outcomeNote == null || outcomeNote.isBlank()) {
            throw new ValidationException("An outcome note is required to close a referral");
        }
        Referral referral = find(id);
        access.checkCanUpdate(user, referral);
        referral.close(actor(user), outcomeNote.trim(), clock.instant());
        audit.record(user.getId(), "REFERRAL_CLOSED", "PATIENT", referral.getPatient().getId(), "Referral #" + id);
        return ReferralDto.detail(referral, clock.instant());
    }

    /**
     * Closes the loop on missed referrals: each open referral past its due time is
     * escalated once and turned into a critical task on the ASHA's due list.
     *
     * @return number of referrals escalated
     */
    @Transactional
    public int escalateOverdue(Instant now) {
        List<Referral> overdue = referrals.findOverdueNotEscalated(OPEN_STATUSES, now);
        int escalated = 0;
        for (Referral referral : overdue) {
            if (referral.escalateIfOverdue(now)) {
                FollowUp task = new FollowUp(
                        referral.getPatient(),
                        referral.getPatient().getAsha(),
                        "Referral to " + referral.getFacility().getName() + " is overdue and the patient has not "
                                + "arrived. Visit today and help them reach the facility. Original reason: "
                                + referral.getReason(),
                        Priority.CRITICAL,
                        now.plus(ESCALATION_FOLLOW_UP_WINDOW),
                        now);
                task.setReferral(referral);
                task.setRule(referral.getRule());
                followUps.save(task);
                escalated++;
            }
        }
        if (escalated > 0) {
            log.info("Escalated {} overdue referral(s)", escalated);
        }
        return escalated;
    }

    /** Once the patient reaches the facility, the ASHA's "help them get there" task is done. */
    private void resolveEscalationTasks(Referral referral, Instant now) {
        followUps.findByPatientIdAndStatus(referral.getPatient().getId(), FollowUpStatus.PENDING).stream()
                .filter(f -> f.getReferral() != null && f.getReferral().getId().equals(referral.getId()))
                .forEach(f -> f.complete(now, "Patient reached " + referral.getFacility().getName()));
    }

    private Referral find(Long id) {
        return referrals.findById(id).orElseThrow(() -> new NotFoundException("Referral", id));
    }

    private AppUser actor(AuthUser user) {
        return users.getReferenceById(user.getId());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
