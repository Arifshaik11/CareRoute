package com.innovation_matrix.careroute.rules.dto;

import java.time.Instant;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.rules.CareRule;
import com.innovation_matrix.careroute.rules.RuleAction;
import com.innovation_matrix.careroute.rules.RuleCondition;

public record RuleDto(
        Long id,
        String code,
        String name,
        Program program,
        RuleCondition condition,
        RuleAction action,
        Priority priority,
        Integer dueWithinHours,
        String explanation,
        boolean active,
        Instant updatedAt) {

    public static RuleDto from(CareRule r) {
        return new RuleDto(r.getId(), r.getCode(), r.getName(), r.getProgram(), r.getCondition(), r.getAction(),
                r.getPriority(), r.getDueWithinHours(), r.getExplanation(), r.isActive(), r.getUpdatedAt());
    }
}
