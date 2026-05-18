package com.travelplan.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.travelplan.auth.domain.AuthRole;
import com.travelplan.auth.domain.AuthUser;
import com.travelplan.auth.jwt.JwtService.InvalidJwtException;
import com.travelplan.auth.jwt.JwtService.ParsedJwt;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("unit-test-secret-32-bytes-minimum-xxxxxxx", "travelplan-test", 5));

    @Test
    void issues_and_parses_token_with_roles() {
        AuthUser user = sampleUser();

        String token = jwtService.issueAccessToken(user);
        ParsedJwt parsed = jwtService.parse(token);

        assertThat(parsed.userId()).isEqualTo(user.getId());
        assertThat(parsed.email()).isEqualTo("alice@example.com");
        assertThat(parsed.roles()).containsExactlyInAnyOrder("ADMIN", "MANAGER");
        assertThat(parsed.expiresAt()).isAfter(java.time.Instant.now());
    }

    @Test
    void rejects_invalid_signature() {
        JwtService otherService = new JwtService(
                new JwtProperties("another-secret-32-bytes-minimum-please-xxxxx", "travelplan-test", 5));
        String token = otherService.issueAccessToken(sampleUser());

        assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void rejects_wrong_issuer() {
        JwtService otherService = new JwtService(
                new JwtProperties("unit-test-secret-32-bytes-minimum-xxxxxxx", "different-issuer", 5));
        String token = otherService.issueAccessToken(sampleUser());

        assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void rejects_malformed_token() {
        assertThatThrownBy(() -> jwtService.parse("not-a-real-jwt"))
                .isInstanceOf(InvalidJwtException.class);
    }

    private AuthUser sampleUser() {
        AuthUser user = new AuthUser();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");

        AuthRole admin = new AuthRole();
        admin.setName("ADMIN");
        AuthRole manager = new AuthRole();
        manager.setName("MANAGER");
        user.setRoles(Set.of(admin, manager));
        return user;
    }
}
