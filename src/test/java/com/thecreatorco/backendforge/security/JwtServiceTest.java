package com.thecreatorco.backendforge.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import com.thecreatorco.backendforge.user.UserEntity;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String VALID_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";
    private static final String ISSUER = "backendforge";
    private static final String AUDIENCE = "backendforge-api";

    @Test
    void acceptsValidSecretAndPerformsTokenRoundtrip() {
        JwtService service = new JwtService(VALID_SECRET, 900, ISSUER, AUDIENCE);
        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                "test@example.com",
                "hashed-password",
                true,
                Instant.now(),
                Instant.now());

        String token = service.generateToken(user);
        assertThat(token).isNotBlank();

        Claims claims = service.parseAndValidate(token);
        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getAudience()).containsExactly(AUDIENCE);
        assertThat(claims.get("token_use", String.class)).isEqualTo("access");
        assertThat(service.getExpirationSeconds()).isEqualTo(900);
        assertThat(service.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void rejectsNullSecret() {
        assertThatThrownBy(() -> new JwtService(null, 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET must be configured with a Base64-encoded secret");
    }

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> new JwtService("", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET must be configured with a Base64-encoded secret");
    }

    @Test
    void rejectsWhitespaceSecret() {
        assertThatThrownBy(() -> new JwtService("   ", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET must be configured with a Base64-encoded secret");
    }

    @Test
    void rejectsInvalidBase64Secret() {
        assertThatThrownBy(() -> new JwtService("not-base64-secret-with-hyphens-and-bad-chars!", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET must be valid Base64");
    }

    @Test
    void rejectsSecretShorterThanMinimum() {
        assertThatThrownBy(() -> new JwtService("c2hvcnQ=", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET must decode to at least 32 bytes");
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 0, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_EXPIRATION_SECONDS must be greater than zero");
    }

    @Test
    void rejectsBlankIssuer() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 900, " ", AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_ISSUER must be configured");
    }

    @Test
    void rejectsBlankAudience() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 900, ISSUER, " "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_AUDIENCE must be configured");
    }
}
