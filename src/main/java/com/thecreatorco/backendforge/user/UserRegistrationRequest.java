package com.thecreatorco.backendforge.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email must not exceed 320 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = UserService.MIN_PASSWORD_LENGTH, max = UserService.MAX_PASSWORD_LENGTH,
                message = "Password must be between 12 and 128 characters")
        @PasswordByteLength
        String password) {
}
