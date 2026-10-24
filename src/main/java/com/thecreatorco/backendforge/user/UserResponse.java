package com.thecreatorco.backendforge.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        boolean enabled,
        Instant createdAt) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.isEnabled(), user.getCreatedAt());
    }
}
