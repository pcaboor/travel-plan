package com.travelplan.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.travelplan.auth.domain.AuthRole;
import com.travelplan.auth.domain.AuthUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;

    public JwtService(JwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes long for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = properties.issuer();
        this.accessTtl = Duration.ofMinutes(properties.accessTokenTtlMinutes());
    }

    public String issueAccessToken(AuthUser user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(AuthRole::getName)
                .toList();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    public ParsedJwt parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return ParsedJwt.fromClaims(jws.getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException("Invalid JWT", e);
        }
    }

    public Duration accessTokenTtl() {
        return accessTtl;
    }

    public record ParsedJwt(UUID userId, String email, List<String> roles, Instant expiresAt) {

        @SuppressWarnings("unchecked")
        static ParsedJwt fromClaims(Claims claims) {
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            Object rolesClaim = claims.get("roles");
            List<String> roles = (rolesClaim instanceof List<?> list)
                    ? list.stream().map(Object::toString).toList()
                    : List.of();
            return new ParsedJwt(userId, email, roles, claims.getExpiration().toInstant());
        }
    }

    public static final class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
