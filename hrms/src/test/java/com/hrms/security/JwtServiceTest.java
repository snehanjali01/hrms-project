package com.hrms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-chars-long";
    private static final String OTHER_SECRET = "a-completely-different-secret-key-of-32-chars-plus";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private final JwtService jwtService = new JwtService(SECRET, ONE_HOUR_MS);

    @Test
    void generateToken_thenParse_shouldReturnTheSameClaims() {
        String token = jwtService.generateToken("neha.singh", 7L, 12L, List.of("HR", "MANAGER"));

        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("neha.singh");
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(7L);
        assertThat(((Number) claims.get("employeeId")).longValue()).isEqualTo(12L);
        List<String> roles = ((List<?>) claims.get("roles")).stream().map(Object::toString).toList();
        assertThat(roles).containsExactly("HR", "MANAGER");
    }

    @Test
    void parseClaims_shouldReject_whenTokenIsExpired() {
        JwtService alreadyExpired = new JwtService(SECRET, -1_000L);
        String token = alreadyExpired.generateToken("neha.singh", 7L, 12L, List.of("HR"));

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parseClaims_shouldReject_whenTokenWasTamperedWith() {
        String token = jwtService.generateToken("neha.singh", 7L, 12L, List.of("EMPLOYEE"));
        String tampered = token.substring(0, token.length() - 5)
                + (token.endsWith("AAAAA") ? "BBBBB" : "AAAAA");

        assertThatThrownBy(() -> jwtService.parseClaims(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseClaims_shouldReject_whenSignedWithADifferentKey() {
        String token = new JwtService(OTHER_SECRET, ONE_HOUR_MS)
                .generateToken("neha.singh", 7L, 12L, List.of("ADMIN"));

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseClaims_shouldReject_whenTokenIsGarbage() {
        assertThatThrownBy(() -> jwtService.parseClaims("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void constructor_shouldFail_whenSecretIsTooShort() {
        assertThatThrownBy(() -> new JwtService("too-short", ONE_HOUR_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32");
    }
}
