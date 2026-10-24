package com.innovation_matrix.careroute.referral;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically escalates referrals whose patient has not reached the facility in time. */
@Component
public class ReferralEscalationJob {

    private final ReferralService referralService;
    private final Clock clock;

    public ReferralEscalationJob(ReferralService referralService, Clock clock) {
        this.referralService = referralService;
        this.clock = clock;
    }

    @Scheduled(initialDelayString = "20000", fixedDelayString = "${app.escalation.interval-ms}")
    public void run() {
        referralService.escalateOverdue(clock.instant());
    }
}
