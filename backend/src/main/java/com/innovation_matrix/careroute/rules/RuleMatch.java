package com.innovation_matrix.careroute.rules;

import com.innovation_matrix.careroute.common.Priority;

/** A rule that fired for a visit, with its explanation already filled in. */
public record RuleMatch(
        CareRule rule,
        RuleAction action,
        Priority priority,
        int dueWithinHours,
        String explanation) {
}
