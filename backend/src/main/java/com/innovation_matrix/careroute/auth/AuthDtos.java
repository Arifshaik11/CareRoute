package com.innovation_matrix.careroute.auth;

import java.time.Instant;

import com.innovation_matrix.careroute.user.AppUser;
import com.innovation_matrix.careroute.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 60) String username,
            @NotBlank @Size(max = 100) String password) {
    }

    public record LoginResponse(String token, Instant expiresAt, UserDto user) {
    }

    public record UserDto(
            Long id,
            String username,
            String fullName,
            Role role,
            Long villageId,
            String villageName,
            Long facilityId,
            String facilityName) {

        /** Must be called inside a transaction (touches lazy associations). */
        public static UserDto from(AppUser u) {
            return new UserDto(
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getVillage() != null ? u.getVillage().getId() : null,
                    u.getVillage() != null ? u.getVillage().getName() : null,
                    u.getFacility() != null ? u.getFacility().getId() : null,
                    u.getFacility() != null ? u.getFacility().getName() : null);
        }
    }
}
