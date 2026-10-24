package com.innovation_matrix.careroute.dashboard;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.referral.dto.ReferralDto;

/** Block-level view for supervisors: where are patients falling out of care? */
public record DashboardDto(
        Instant generatedAt,
        Kpis kpis,
        Funnel funnel,
        List<VillageRow> villages,
        Map<Program, Long> patientsByProgram,
        List<ReferralDto> activeEscalations) {

    public record Kpis(
            long patients,
            long visitsLast7Days,
            long pendingFollowUps,
            long overdueFollowUps,
            long openReferrals,
            long escalatedReferrals,
            Double referralCompletionRate) {
    }

    /** How many referrals reached each stage of the loop. */
    public record Funnel(long referred, long acknowledged, long arrived, long closed, long lostOrOverdue) {
    }

    public record VillageRow(
            Long villageId,
            String villageName,
            String ashaName,
            long patients,
            long pendingFollowUps,
            long overdueFollowUps,
            long openReferrals,
            long escalatedReferrals,
            Double referralCompletionRate) {
    }
}
