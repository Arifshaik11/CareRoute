package com.innovation_matrix.careroute.notification;

import java.util.Map;

import com.innovation_matrix.careroute.referral.Referral;
import com.innovation_matrix.careroute.sse.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SseService sseService;
    private final SmsService smsService;

    public NotificationService(SseService sseService, SmsService smsService) {
        this.sseService = sseService;
        this.smsService = smsService;
    }

    public void onReferralEscalated(Referral referral) {
        var payload = Map.of(
                "referralId", referral.getId(),
                "patientName", referral.getPatient().getFullName(),
                "villageName", referral.getPatient().getVillage().getName(),
                "reason", referral.getReason()
        );
        sseService.pushToAll("escalation", payload);

        String ashaPhone = referral.getPatient().getAsha().getPhone();
        if (ashaPhone != null && !ashaPhone.isBlank()) {
            String msg = "CareRoute Alert: " + referral.getPatient().getFullName()
                    + " referral is overdue. Please visit today. (Ref #" + referral.getId() + ")";
            smsService.send(ashaPhone, msg);
        }
        log.info("Notification sent for escalated referral #{}", referral.getId());
    }
}
