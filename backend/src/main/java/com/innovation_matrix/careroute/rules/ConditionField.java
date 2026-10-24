package com.innovation_matrix.careroute.rules;

import java.util.EnumSet;
import java.util.Set;

/** Observations a rule may test. Each field has a kind that fixes which operators are valid. */
public enum ConditionField {
    SYSTOLIC_BP("Systolic BP (mmHg)", Kind.NUMBER),
    DIASTOLIC_BP("Diastolic BP (mmHg)", Kind.NUMBER),
    TEMPERATURE_C("Temperature (°C)", Kind.NUMBER),
    WEIGHT_KG("Weight (kg)", Kind.NUMBER),
    PREGNANCY_WEEKS("Pregnancy (weeks)", Kind.NUMBER),
    AGE("Age (years)", Kind.NUMBER),
    DOSE_TAKEN("Dose taken", Kind.BOOLEAN),
    SYMPTOMS("Reported symptoms", Kind.SYMPTOM_LIST);

    public enum Kind {
        NUMBER(EnumSet.of(Operator.GT, Operator.GTE, Operator.LT, Operator.LTE, Operator.EQ, Operator.NEQ)),
        BOOLEAN(EnumSet.of(Operator.EQ, Operator.NEQ)),
        SYMPTOM_LIST(EnumSet.of(Operator.CONTAINS, Operator.NOT_CONTAINS));

        private final Set<Operator> operators;

        Kind(Set<Operator> operators) {
            this.operators = operators;
        }

        public Set<Operator> getOperators() {
            return operators;
        }
    }

    private final String label;
    private final Kind kind;

    ConditionField(String label, Kind kind) {
        this.label = label;
        this.kind = kind;
    }

    public String getLabel() {
        return label;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean supports(Operator op) {
        return kind.getOperators().contains(op);
    }
}
