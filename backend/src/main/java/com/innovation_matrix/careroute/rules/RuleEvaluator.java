package com.innovation_matrix.careroute.rules;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.innovation_matrix.careroute.visit.Symptom;
import org.springframework.stereotype.Component;

/**
 * Deterministic, explainable rule evaluation. No learning, no hidden weights:
 * a rule fires only when its stated condition holds, and a missing observation
 * never satisfies a clause.
 */
@Component
public class RuleEvaluator {

    public List<RuleMatch> evaluate(Collection<CareRule> rules, VisitFacts facts) {
        return rules.stream()
                .filter(CareRule::isActive)
                .filter(rule -> rule.appliesTo(facts.program()))
                .filter(rule -> matches(rule.getCondition(), facts))
                .map(rule -> new RuleMatch(
                        rule,
                        rule.getAction(),
                        rule.getPriority(),
                        rule.getDueWithinHours(),
                        renderExplanation(rule.getExplanation(), facts)))
                .toList();
    }

    public boolean matches(RuleCondition condition, VisitFacts facts) {
        if (condition == null || condition.clauses() == null || condition.clauses().isEmpty()) {
            return false;
        }
        return condition.match() == MatchMode.ALL
                ? condition.clauses().stream().allMatch(c -> clauseMatches(c, facts))
                : condition.clauses().stream().anyMatch(c -> clauseMatches(c, facts));
    }

    boolean clauseMatches(RuleCondition.Clause clause, VisitFacts facts) {
        Object actual = facts.valueOf(clause.field());
        JsonNode expected = clause.value();
        if (actual == null || expected == null || expected.isNull()) {
            return false;
        }
        return switch (clause.field().getKind()) {
            case NUMBER -> compareNumbers((BigDecimal) actual, clause.op(), expected.decimalValue());
            case BOOLEAN -> compareBooleans((Boolean) actual, clause.op(), expected.asBoolean());
            case SYMPTOM_LIST -> compareSymptoms(asStringSet(actual), clause.op(), expected.asText());
        };
    }

    private static boolean compareNumbers(BigDecimal actual, Operator op, BigDecimal expected) {
        int cmp = actual.compareTo(expected);
        return switch (op) {
            case GT -> cmp > 0;
            case GTE -> cmp >= 0;
            case LT -> cmp < 0;
            case LTE -> cmp <= 0;
            case EQ -> cmp == 0;
            case NEQ -> cmp != 0;
            default -> false;
        };
    }

    private static boolean compareBooleans(Boolean actual, Operator op, boolean expected) {
        return switch (op) {
            case EQ -> actual == expected;
            case NEQ -> actual != expected;
            default -> false;
        };
    }

    private static boolean compareSymptoms(Set<String> actual, Operator op, String expected) {
        return switch (op) {
            case CONTAINS -> actual.contains(expected);
            case NOT_CONTAINS -> !actual.contains(expected);
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private static Set<String> asStringSet(Object value) {
        return (Set<String>) value;
    }

    /** Replaces {FIELD} placeholders with the observed values, e.g. "{SYSTOLIC_BP}" → "150". */
    public String renderExplanation(String template, VisitFacts facts) {
        if (template == null) {
            return "";
        }
        String result = template;
        for (ConditionField field : ConditionField.values()) {
            String token = "{" + field.name() + "}";
            if (result.contains(token)) {
                result = result.replace(token, format(field, facts.valueOf(field)));
            }
        }
        return result;
    }

    private static String format(ConditionField field, Object value) {
        if (value == null) {
            return "not recorded";
        }
        return switch (field.getKind()) {
            case NUMBER -> ((BigDecimal) value).stripTrailingZeros().toPlainString();
            case BOOLEAN -> Boolean.TRUE.equals(value) ? "yes" : "no";
            case SYMPTOM_LIST -> {
                Set<String> codes = asStringSet(value);
                yield codes.isEmpty() ? "none" : codes.stream()
                        .sorted()
                        .map(RuleEvaluator::symptomLabel)
                        .collect(Collectors.joining(", "));
            }
        };
    }

    private static String symptomLabel(String code) {
        try {
            return Symptom.valueOf(code).getLabel().toLowerCase();
        } catch (IllegalArgumentException ex) {
            return code;
        }
    }
}
