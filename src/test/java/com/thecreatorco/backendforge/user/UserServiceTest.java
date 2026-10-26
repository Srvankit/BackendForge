package com.thecreatorco.backendforge.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thecreatorco.backendforge.security.JwtService;

class UserServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "correct-horse-battery";

    @Test
    void doesNotClassifyUnrelatedConstraintViolationAsDuplicateEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        UserService userService = new UserService(userRepository, passwordEncoder, jwtService);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        ConstraintViolationException databaseViolation = new ConstraintViolationException(
                "password hash must not be blank",
                null,
                "ck_users_password_hash_not_blank");
        DataIntegrityViolationException persistenceViolation =
                new DataIntegrityViolationException("constraint violation", databaseViolation);
        when(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(UserEntity.class)))
                .thenThrow(persistenceViolation);

        assertThatThrownBy(() -> userService.register(
                new UserRegistrationRequest(EMAIL, PASSWORD)))
                .isSameAs(persistenceViolation)
                .isNotInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void stillVerifiesPasswordWhenEmailIsUnknown() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        UserService userService = new UserService(userRepository, passwordEncoder, jwtService);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.matches(eq(PASSWORD), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(new UserLoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder).matches(eq(PASSWORD), anyString());
    }
}
