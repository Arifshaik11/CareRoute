package com.innovation_matrix.careroute.patient.dto;

import com.innovation_matrix.careroute.user.Role;

/** Data minimisation: supervisors work with aggregates and don't need patients' phone numbers. */
public final class PhoneMasking {

    private PhoneMasking() {
    }

    public static String forRole(String phone, Role viewerRole) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return viewerRole == Role.SUPERVISOR ? mask(phone) : phone;
    }

    static String mask(String phone) {
        if (phone.length() <= 4) {
            return "****";
        }
        return phone.substring(0, 2) + "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 2);
    }
}
