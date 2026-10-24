package com.innovation_matrix.careroute.common;

/** Urgency of a follow-up or referral, ordered from lowest to highest. */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public boolean isHigherThan(Priority other) {
        return this.ordinal() > other.ordinal();
    }
}
