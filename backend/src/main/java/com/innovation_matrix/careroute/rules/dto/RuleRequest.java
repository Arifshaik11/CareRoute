package com.innovation_matrix.careroute.rules.dto;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.rules.RuleAction;
import com.innovation_matrix.careroute.rules.RuleCondition;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Create or update a rule. The condition's structure is checked by RuleConditionValidator. */
public record RuleRequest(
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,59}$",
                message = "use 3-60 upper-case letters, digits or underscores") String code,
        @NotBlank @Size(max = 160) String name,
        Program program,
        @NotNull RuleCondition condition,
        @NotNull RuleAction action,
        @NotNull Priority priority,
        @NotNull @Min(1) @Max(2160) Integer dueWithinHours,
        @NotBlank @Size(max = 500) String explanation,
        boolean active) {
}
