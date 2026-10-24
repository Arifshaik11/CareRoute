package com.innovation_matrix.careroute.careplan;

import java.time.Instant;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.rules.RuleAction;

/** What one fired rule led to — shown to the ASHA right after saving a visit. */
public record RuleOutcome(
        String ruleCode,
        String ruleName,
        RuleAction action,
        Priority priority,
        String explanation,
        Result result,
        Instant dueAt,
        Long followUpId,
        Long referralId) {

    public enum Result {
        /** A task was added to the ASHA's due list. */
        FOLLOW_UP_CREATED,
        /** A new referral was sent to the facility. */
        REFERRAL_CREATED,
        /** Another, more urgent rule in the same visit already created the referral. */
        COVERED_BY_REFERRAL,
        /** The patient already has an open referral; no duplicate was created. */
        ALREADY_REFERRED
    }
}
