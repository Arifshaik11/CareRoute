package com.innovation_matrix.careroute.followup;

import java.time.Duration;
import java.time.Instant;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.common.error.InvalidStateException;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.rules.CareRule;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.visit.Visit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A task on an ASHA's due list. */
@Entity
@Table(name = "follow_up")
public class FollowUp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private AppUser assignedTo;

    /** The visit whose observations created this task, if any. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    /** The rule that created this task, if any. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private CareRule rule;

    /** Set when the task was created by a referral escalation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id")
    private Referral referral;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private FollowUpStatus status = FollowUpStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_note", columnDefinition = "text")
    private String completionNote;

    protected FollowUp() {
    }

    public FollowUp(Patient patient, AppUser assignedTo, String reason, Priority priority,
                    Instant dueAt, Instant createdAt) {
        this.patient = patient;
        this.assignedTo = assignedTo;
        this.reason = reason;
        this.priority = priority;
        this.dueAt = dueAt;
        setCreatedAt(createdAt);
    }

    public void complete(Instant at, String note) {
        if (status != FollowUpStatus.PENDING) {
            throw new InvalidStateException("Only pending follow-ups can be completed");
        }
        this.status = FollowUpStatus.COMPLETED;
        this.completedAt = at;
        this.completionNote = note;
    }

    public boolean isOverdue(Instant now) {
        return status == FollowUpStatus.PENDING && dueAt.isBefore(now);
    }

    /** Demo support: pretend time has moved forward. */
    public void shiftDueAt(Duration amount) {
        this.dueAt = dueAt.minus(amount);
    }

    public Patient getPatient() {
        return patient;
    }

    public AppUser getAssignedTo() {
        return assignedTo;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public CareRule getRule() {
        return rule;
    }

    public void setRule(CareRule rule) {
        this.rule = rule;
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public String getReason() {
        return reason;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getCompletionNote() {
        return completionNote;
    }
}
