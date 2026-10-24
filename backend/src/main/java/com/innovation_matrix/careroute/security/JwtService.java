package com.innovation_matrix.careroute.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.innovation_matrix.careroute.config.AppProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

/** Issues and verifies HMAC-signed access tokens. */
@Service
public class JwtService {

    private final SecretKey key;
    private final AppProperties.Jwt config;
    private final Clock clock;

    public JwtService(AppProperties properties, Clock clock) {
        this.config = properties.jwt();
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(config.secret()));
        this.clock = clock;
    }

    public IssuedToken issue(AuthUser user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(config.expiration());
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /** @return the username in a valid, unexpired token; empty if the token is invalid. */
    public Optional<String> verifyAndGetUsername(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(subject);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
