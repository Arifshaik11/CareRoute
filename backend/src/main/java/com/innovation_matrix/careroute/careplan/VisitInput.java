package com.innovation_matrix.careroute.careplan;

import java.math.BigDecimal;
import java.util.List;

import com.innovation_matrix.careroute.visit.VisitSource;

/** Observations captured at a visit, independent of how they arrived (form, voice, seed data). */
public record VisitInput(
        Integer systolicBp,
        Integer diastolicBp,
        BigDecimal weightKg,
        BigDecimal temperatureC,
        Integer pregnancyWeeks,
        Boolean doseTaken,
        List<String> symptoms,
        String notes,
        VisitSource source) {
}
