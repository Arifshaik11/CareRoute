package com.innovation_matrix.careroute.rules;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A rule's condition, stored as JSON in care_rule.condition, e.g.
 * {"match":"ANY","clauses":[{"field":"SYSTOLIC_BP","op":"GTE","value":140}]}.
 */
public record RuleCondition(MatchMode match, List<Clause> clauses) {

    /** One comparison: observed field, operator, expected value (number, boolean or symptom code). */
    public record Clause(ConditionField field, Operator op, JsonNode value) {
    }
}
