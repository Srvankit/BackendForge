package com.thecreatorco.backendforge.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String VALID_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";
    private static final String ISSUER = "backendforge";
    private static final String AUDIENCE = "backendforge-api";

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> new JwtService("", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsInvalidBase64Secret() {
        assertThatThrownBy(() -> new JwtService("not-base64", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsSecretShorterThanMinimum() {
        assertThatThrownBy(() -> new JwtService("c2hvcnQ=", 900, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 0, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsBlankIssuer() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 900, " ", AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsBlankAudience() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 900, ISSUER, " "))
                .isInstanceOf(IllegalStateException.class);
    }
}
