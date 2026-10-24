package com.innovation_matrix.careroute.referral;

import java.time.Instant;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One immutable entry in a referral's timeline. */
@Entity
@Table(name = "referral_event")
public class ReferralEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referral_id", nullable = false)
    private Referral referral;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private ReferralEventType eventType;

    /** Null for system events such as automatic escalation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private AppUser actor;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ReferralEvent() {
    }

    ReferralEvent(Referral referral, ReferralEventType eventType, AppUser actor, String note, Instant occurredAt) {
        this.referral = referral;
        this.eventType = eventType;
        this.actor = actor;
        this.note = note;
        this.occurredAt = occurredAt;
        setCreatedAt(occurredAt);
    }

    public Referral getReferral() {
        return referral;
    }

    public ReferralEventType getEventType() {
        return eventType;
    }

    public AppUser getActor() {
        return actor;
    }

    public String getNote() {
        return note;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
