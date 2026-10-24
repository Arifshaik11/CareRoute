package com.innovation_matrix.careroute.referral.dto;

import jakarta.validation.constraints.Size;

/** Optional note for acknowledge / arrive; required (non-blank) for close. */
public record ReferralActionRequest(@Size(max = 500) String note) {
}
