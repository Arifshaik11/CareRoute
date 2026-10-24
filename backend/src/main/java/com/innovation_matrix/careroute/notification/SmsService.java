package com.innovation_matrix.careroute.notification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Sends SMS alerts via Twilio. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and
 * TWILIO_FROM_NUMBER to enable; if unset, messages are logged only.
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${app.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.twilio.auth-token:}")
    private String authToken;

    @Value("${app.twilio.from-number:}")
    private String fromNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String toNumber, String message) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.info("[SMS-MOCK] → {} : {}", toNumber, message);
            return;
        }
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String creds = Base64.getEncoder()
                    .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + creds);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("From", fromNumber);
            body.add("To", toNumber);
            body.add("Body", message);

            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("[SMS-SENT] → {}", toNumber);
        } catch (Exception e) {
            log.warn("[SMS-FAILED] → {} : {}", toNumber, e.getMessage());
        }
    }
}
