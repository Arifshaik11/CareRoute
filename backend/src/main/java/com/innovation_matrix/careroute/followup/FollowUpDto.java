package com.innovation_matrix.careroute.followup;

import java.time.Instant;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;

/** A follow-up task, with enough patient context to render a due-list card. */
public record FollowUpDto(
        Long id,
        Long patientId,
        String patientName,
        Integer patientAge,
        Program program,
        String villageName,
        String reason,
        Priority priority,
        Instant dueAt,
        FollowUpStatus status,
        boolean overdue,
        Instant completedAt,
        String completionNote,
        String ruleCode,
        String ruleName,
        Long referralId,
        Instant createdAt) {

    /** Call inside a transaction. */
    public static FollowUpDto from(FollowUp f, Instant now) {
        var p = f.getPatient();
        return new FollowUpDto(
                f.getId(),
                p.getId(),
                p.getFullName(),
                p.getAge(),
                p.getProgram(),
                p.getVillage().getName(),
                f.getReason(),
                f.getPriority(),
                f.getDueAt(),
                f.getStatus(),
                f.isOverdue(now),
                f.getCompletedAt(),
                f.getCompletionNote(),
                f.getRule() != null ? f.getRule().getCode() : null,
                f.getRule() != null ? f.getRule().getName() : null,
                f.getReferral() != null ? f.getReferral().getId() : null,
                f.getCreatedAt());
    }
}
