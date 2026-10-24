package com.innovation_matrix.careroute.referral;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.common.error.InvalidStateException;
import com.innovation_matrix.careroute.location.Facility;
import com.innovation_matrix.careroute.patient.Patient;
import com.innovation_matrix.careroute.rules.CareRule;
import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.visit.Visit;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * A referral from the community to a facility. Every state change goes through
 * this aggregate so the timeline (events) and timestamps can never disagree.
 */
@Entity
@Table(name = "referral")
public class Referral extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private CareRule rule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referred_by", nullable = false)
    private AppUser referredBy;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ReferralStatus status = ReferralStatus.REFERRED;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "arrived_at")
    private Instant arrivedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "outcome_note", columnDefinition = "text")
    private String outcomeNote;

    @OneToMany(mappedBy = "referral", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt asc")
    private List<ReferralEvent> events = new ArrayList<>();

    protected Referral() {
    }

    public static Referral create(Patient patient, Visit visit, CareRule rule, Facility facility, AppUser referredBy,
                                  String reason, Priority priority, Instant referredAt, Instant dueAt) {
        Referral r = new Referral();
        r.patient = patient;
        r.visit = visit;
        r.rule = rule;
        r.facility = facility;
        r.referredBy = referredBy;
        r.reason = reason;
        r.priority = priority;
        r.dueAt = dueAt;
        r.setCreatedAt(referredAt);
        r.addEvent(ReferralEventType.REFERRED, referredBy, reason, referredAt);
        return r;
    }

    public void acknowledge(AppUser actor, String note, Instant at) {
        transition(ReferralStatus.ACKNOWLEDGED);
        this.acknowledgedAt = at;
        addEvent(ReferralEventType.ACKNOWLEDGED, actor, note, at);
    }

    public void markArrived(AppUser actor, String note, Instant at) {
        transition(ReferralStatus.ARRIVED);
        if (acknowledgedAt == null) {
            acknowledgedAt = at;
        }
        this.arrivedAt = at;
        addEvent(ReferralEventType.ARRIVED, actor, note, at);
    }

    public void close(AppUser actor, String outcomeNote, Instant at) {
        transition(ReferralStatus.CLOSED);
        this.closedAt = at;
        this.outcomeNote = outcomeNote;
        addEvent(ReferralEventType.CLOSED, actor, outcomeNote, at);
    }

    /** @return true if this call escalated the referral (it was open, overdue and not yet escalated). */
    public boolean escalateIfOverdue(Instant now) {
        if (!status.isOpen() || escalatedAt != null || !dueAt.isBefore(now)) {
            return false;
        }
        this.escalatedAt = now;
        addEvent(ReferralEventType.ESCALATED, null,
                "Patient had not reached " + facility.getName() + " by the due time", now);
        return true;
    }

    public boolean isOverdue(Instant now) {
        return status.isOpen() && dueAt.isBefore(now);
    }

    /** Demo support: pretend time has moved forward. */
    public void shiftDueAt(Duration amount) {
        this.dueAt = dueAt.minus(amount);
    }

    private void transition(ReferralStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidStateException("Referral cannot move from " + status + " to " + next);
        }
        this.status = next;
    }

    private void addEvent(ReferralEventType type, AppUser actor, String note, Instant at) {
        events.add(new ReferralEvent(this, type, actor, note, at));
    }

    public Patient getPatient() {
        return patient;
    }

    public Visit getVisit() {
        return visit;
    }

    public CareRule getRule() {
        return rule;
    }

    public Facility getFacility() {
        return facility;
    }

    public AppUser getReferredBy() {
        return referredBy;
    }

    public String getReason() {
        return reason;
    }

    public Priority getPriority() {
        return priority;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public Instant getArrivedAt() {
        return arrivedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }

    public String getOutcomeNote() {
        return outcomeNote;
    }

    public List<ReferralEvent> getEvents() {
        return events;
    }
}
