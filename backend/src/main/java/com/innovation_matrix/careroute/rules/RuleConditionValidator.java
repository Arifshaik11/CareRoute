package com.innovation_matrix.careroute.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.innovation_matrix.careroute.common.error.ValidationException;
import com.innovation_matrix.careroute.visit.Symptom;
import org.springframework.stereotype.Component;

/** Rejects rule conditions that could never be evaluated meaningfully. */
@Component
public class RuleConditionValidator {

    static final int MAX_CLAUSES = 10;

    public void validate(RuleCondition condition) {
        if (condition == null || condition.match() == null) {
            throw new ValidationException("Condition must say whether ALL or ANY clauses must match");
        }
        if (condition.clauses() == null || condition.clauses().isEmpty()) {
            throw new ValidationException("Condition needs at least one clause");
        }
        if (condition.clauses().size() > MAX_CLAUSES) {
            throw new ValidationException("Condition can have at most " + MAX_CLAUSES + " clauses");
        }
        for (int i = 0; i < condition.clauses().size(); i++) {
            validateClause(condition.clauses().get(i), i + 1);
        }
    }

    private void validateClause(RuleCondition.Clause clause, int position) {
        String where = "Clause " + position + ": ";
        if (clause == null || clause.field() == null || clause.op() == null) {
            throw new ValidationException(where + "field and operator are required");
        }
        if (!clause.field().supports(clause.op())) {
            throw new ValidationException(where + "operator " + clause.op() + " cannot be used with " + clause.field());
        }
        JsonNode value = clause.value();
        if (value == null || value.isNull()) {
            throw new ValidationException(where + "a value is required");
        }
        switch (clause.field().getKind()) {
            case NUMBER -> {
                if (!value.isNumber()) {
                    throw new ValidationException(where + "value must be a number");
                }
            }
            case BOOLEAN -> {
                if (!value.isBoolean()) {
                    throw new ValidationException(where + "value must be true or false");
                }
            }
            case SYMPTOM_LIST -> {
                if (!value.isTextual() || !isSymptom(value.asText())) {
                    throw new ValidationException(where + "value must be a known symptom code");
                }
            }
        }
    }

    private static boolean isSymptom(String code) {
        for (Symptom s : Symptom.values()) {
            if (s.name().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
