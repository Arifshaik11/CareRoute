package com.innovation_matrix.careroute.rules;

import java.math.BigDecimal;
import java.util.Set;

import com.innovation_matrix.careroute.patient.Program;

/** The observations a rule is evaluated against. Missing values are null. */
public record VisitFacts(
        Program program,
        Integer age,
        Integer systolicBp,
        Integer diastolicBp,
        BigDecimal temperatureC,
        BigDecimal weightKg,
        Integer pregnancyWeeks,
        Boolean doseTaken,
        Set<String> symptoms) {

    public VisitFacts {
        symptoms = symptoms == null ? Set.of() : Set.copyOf(symptoms);
    }

    /** Numbers are returned as BigDecimal, booleans as Boolean, symptoms as Set&lt;String&gt;. */
    public Object valueOf(ConditionField field) {
        return switch (field) {
            case SYSTOLIC_BP -> toDecimal(systolicBp);
            case DIASTOLIC_BP -> toDecimal(diastolicBp);
            case TEMPERATURE_C -> temperatureC;
            case WEIGHT_KG -> weightKg;
            case PREGNANCY_WEEKS -> toDecimal(pregnancyWeeks);
            case AGE -> toDecimal(age);
            case DOSE_TAKEN -> doseTaken;
            case SYMPTOMS -> symptoms;
        };
    }

    private static BigDecimal toDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
