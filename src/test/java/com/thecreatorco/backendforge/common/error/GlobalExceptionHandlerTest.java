package com.thecreatorco.backendforge.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.thecreatorco.backendforge.ratelimit.RateLimitExceededException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(OutputCaptureExtension.class)
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

    @Test
    void returnsTooManyRequestsWithRetryAfterHeader() {
        var response = handler.handleRateLimitExceeded(new RateLimitExceededException(7));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("7");
        assertThat(response.getBody())
                .extracting(ApiError::code, ApiError::message, ApiError::fieldErrors)
                .containsExactly("RATE_LIMIT_EXCEEDED", "Too many requests", Map.of());
    }

    @Test
    void logsDiagnosticInfoForRateLimitWithoutSensitiveData(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("198.51.100.5");
        request.addHeader("Authorization", "Bearer sensitive-token-12345");
        request.setContent("{\"password\":\"sensitive-password-67890\"}".getBytes());

        handler.handleRateLimitExceeded(new RateLimitExceededException(45), request);

        assertThat(output.getOut())
                .contains("Rate limit exceeded for POST /api/v1/auth/login from remote address: 198.51.100.5, retry-after: 45s")
                .doesNotContain("sensitive-token-12345")
                .doesNotContain("sensitive-password-67890");
    }

    @Test
    void logsDiagnosticInfoForUnexpectedErrorWithoutSensitiveData(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/register");
        request.addHeader("Authorization", "Bearer sensitive-jwt-token");
        request.setContent("{\"password\":\"top-secret-cleartext\"}".getBytes());

        RuntimeException exceptionWithSensitiveMessage =
                new RuntimeException("Database error: password=db-secret-password connection failure");

        handler.handleUnexpected(exceptionWithSensitiveMessage, request);

        assertThat(output.getOut())
                .contains("Unexpected error while processing request POST /api/v1/auth/register: java.lang.RuntimeException")
                .doesNotContain("db-secret-password")
                .doesNotContain("sensitive-jwt-token")
                .doesNotContain("top-secret-cleartext");
    }
}
