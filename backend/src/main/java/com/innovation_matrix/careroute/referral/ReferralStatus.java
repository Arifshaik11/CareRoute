package com.innovation_matrix.careroute.referral;

/**
 * Closed-loop referral lifecycle:
 * REFERRED → ACKNOWLEDGED → ARRIVED → CLOSED.
 * A referral is "open" until the patient arrives; open referrals past their due time escalate.
 */
public enum ReferralStatus {
    REFERRED,
    ACKNOWLEDGED,
    ARRIVED,
    CLOSED;

    public boolean isOpen() {
        return this == REFERRED || this == ACKNOWLEDGED;
    }

    public boolean canMoveTo(ReferralStatus next) {
        return switch (this) {
            case REFERRED -> next == ACKNOWLEDGED || next == ARRIVED;
            case ACKNOWLEDGED -> next == ARRIVED;
            case ARRIVED -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}
