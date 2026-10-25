package com.thecreatorco.backendforge.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordByteLengthValidatorTest {

    private final PasswordByteLengthValidator validator = new PasswordByteLengthValidator();

    @Test
    void acceptsPasswordAtBcryptByteBoundary() {
        assertThat(validator.isValid("a".repeat(72), null)).isTrue();
    }

    @Test
    void rejectsAsciiPasswordAboveBcryptByteBoundary() {
        assertThat(validator.isValid("a".repeat(73), null)).isFalse();
    }

    @Test
    void measuresMultibytePasswordsUsingUtf8Bytes() {
        assertThat(validator.isValid("😀".repeat(18), null)).isTrue();
        assertThat(validator.isValid("😀".repeat(19), null)).isFalse();
    }
}
