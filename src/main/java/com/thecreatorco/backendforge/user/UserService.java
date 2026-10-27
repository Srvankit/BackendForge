package com.thecreatorco.backendforge.user;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thecreatorco.backendforge.security.JwtService;

@Service
public class UserService {

    public static final int MIN_PASSWORD_LENGTH = 12;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_PASSWORD_BYTES = 72;

    private static final String TIMING_EQUALIZATION_PASSWORD = "backendforge-timing-equalization";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String dummyPasswordHash;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyPasswordHash = passwordEncoder.encode(TIMING_EQUALIZATION_PASSWORD);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(UserLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = userRepository.findByEmail(normalizedEmail).orElse(null);
        String passwordHash = user != null ? user.getPasswordHash() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (user == null || !user.isEnabled() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.generateToken(user),
                jwtService.getTokenType(),
                jwtService.getExpirationSeconds());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserEntity user) {
        if (user == null || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        validatePassword(request.password());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        Instant now = Instant.now();
        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                true,
                now,
                now);

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueConstraintViolation(exception)) {
                throw new DuplicateEmailException();
            }
            throw exception;
        }
    }

    private boolean isEmailUniqueConstraintViolation(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation
                    && "uq_users_email".equals(constraintViolation.getConstraintName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return normalizedEmail;
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH
                || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "Password must be between 12 and 128 characters and no more than "
                            + MAX_PASSWORD_BYTES + " UTF-8 bytes");
        }
    }
}
