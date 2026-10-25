package com.thecreatorco.backendforge.user;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordByteLengthValidator
        implements ConstraintValidator<PasswordByteLength, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password == null
                || password.getBytes(StandardCharsets.UTF_8).length <= UserService.MAX_PASSWORD_BYTES;
    }
}
