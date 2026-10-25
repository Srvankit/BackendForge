package com.thecreatorco.backendforge.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void doesNotExposeIllegalArgumentMessage() {
        var response = handler.handleIllegalArgument(
                new IllegalArgumentException("internal validation detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .extracting(ApiError::code, ApiError::message, ApiError::fieldErrors)
                .containsExactly("VALIDATION_ERROR", "Request contains invalid values", Map.of());
    }

    @Test
    void returnsStableUnexpectedErrorResponse() {
        var response = handler.handleUnexpected(new IllegalStateException("internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .extracting(ApiError::code, ApiError::message, ApiError::fieldErrors)
                .containsExactly(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        Map.of());
    }
}
