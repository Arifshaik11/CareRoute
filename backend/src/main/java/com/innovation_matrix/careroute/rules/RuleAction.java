package com.innovation_matrix.careroute.rules;

/** What happens when a rule fires. */
public enum RuleAction {
    /** Add a task to the ASHA's due list. */
    FOLLOW_UP,
    /** Refer the patient to the village's facility. */
    REFERRAL
}
