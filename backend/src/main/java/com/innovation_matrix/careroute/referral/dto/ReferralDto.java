package com.innovation_matrix.careroute.referral.dto;

import java.time.Instant;
import java.util.List;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.referral.ReferralEvent;
import com.innovation_matrix.careroute.referral.ReferralEventType;
import com.innovation_matrix.careroute.referral.ReferralStatus;

public record ReferralDto(
        Long id,
        Long patientId,
        String patientName,
        Integer patientAge,
        Program program,
        String villageName,
        String ashaName,
        String facilityName,
        String reason,
        Priority priority,
        ReferralStatus status,
        Instant createdAt,
        Instant dueAt,
        boolean overdue,
        Instant escalatedAt,
        Instant acknowledgedAt,
        Instant arrivedAt,
        Instant closedAt,
        String outcomeNote,
        String ruleCode,
        List<EventDto> events) {

    public record EventDto(ReferralEventType type, String actorName, String note, Instant occurredAt) {

        public static EventDto from(ReferralEvent e) {
            return new EventDto(e.getEventType(),
                    e.getActor() != null ? e.getActor().getFullName() : "CareRoute (automatic)",
                    e.getNote(), e.getOccurredAt());
        }
    }

    /** List view, without the timeline. Call inside a transaction. */
    public static ReferralDto summary(Referral r, Instant now) {
        return of(r, now, null);
    }

    /** Detail view with the full timeline. Call inside a transaction. */
    public static ReferralDto detail(Referral r, Instant now) {
        return of(r, now, r.getEvents().stream().map(EventDto::from).toList());
    }

    private static ReferralDto of(Referral r, Instant now, List<EventDto> events) {
        var p = r.getPatient();
        return new ReferralDto(
                r.getId(),
                p.getId(),
                p.getFullName(),
                p.getAge(),
                p.getProgram(),
                p.getVillage().getName(),
                p.getAsha().getFullName(),
                r.getFacility().getName(),
                r.getReason(),
                r.getPriority(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getDueAt(),
                r.isOverdue(now),
                r.getEscalatedAt(),
                r.getAcknowledgedAt(),
                r.getArrivedAt(),
                r.getClosedAt(),
                r.getOutcomeNote(),
                r.getRule() != null ? r.getRule().getCode() : null,
                events);
    }
}
