package com.innovation_matrix.careroute.security;

import java.util.Objects;

import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Record-level access rules (role checks alone are not enough):
 * <ul>
 *   <li>ASHA — only patients assigned to her.</li>
 *   <li>PHC staff — patients from villages their facility serves, and referrals to that facility.</li>
 *   <li>Supervisor — read access to everything in the block.</li>
 * </ul>
 * Must be called inside a transaction (touches lazy associations).
 */
@Component
public class AccessPolicy {

    public void checkCanView(AuthUser user, Patient patient) {
        boolean allowed = switch (user.getRole()) {
            case ASHA -> Objects.equals(patient.getAsha().getId(), user.getId());
            case PHC_STAFF -> Objects.equals(patient.getVillage().getFacility().getId(), user.getFacilityId());
            case SUPERVISOR -> true;
        };
        if (!allowed) {
            throw new AccessDeniedException("Not allowed to view patient " + patient.getId());
        }
    }

    public void checkCanRecordVisit(AuthUser user, Patient patient) {
        if (user.getRole() != Role.ASHA
                || !Objects.equals(patient.getAsha().getId(), user.getId())) {
            throw new AccessDeniedException("Only the assigned ASHA can record visits for this patient");
        }
    }

    public void checkCanView(AuthUser user, Referral referral) {
        boolean allowed = switch (user.getRole()) {
            case ASHA -> Objects.equals(referral.getPatient().getAsha().getId(), user.getId());
            case PHC_STAFF -> Objects.equals(referral.getFacility().getId(), user.getFacilityId());
            case SUPERVISOR -> true;
        };
        if (!allowed) {
            throw new AccessDeniedException("Not allowed to view referral " + referral.getId());
        }
    }

    public void checkCanUpdate(AuthUser user, Referral referral) {
        if (user.getRole() != Role.PHC_STAFF
                || !Objects.equals(referral.getFacility().getId(), user.getFacilityId())) {
            throw new AccessDeniedException("Only staff at the receiving facility can update this referral");
        }
    }
}
