package com.thecreatorco.backendforge.common.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.thecreatorco.backendforge.ratelimit.RateLimitExceededException;
import com.thecreatorco.backendforge.user.DuplicateEmailException;
import com.thecreatorco.backendforge.user.InvalidCredentialsException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException exception) {
        String code = hasUnknownJsonProperty(exception)
                ? "UNKNOWN_JSON_PROPERTY"
                : "MALFORMED_JSON";
        String message = hasUnknownJsonProperty(exception)
                ? "Request contains an unknown property"
                : "Request body could not be parsed";
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                code,
                message,
                Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception) {
        return errorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Request content type is not supported",
                Map.of());
    }

    private boolean hasUnknownJsonProperty(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof UnrecognizedPropertyException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request contains invalid values",
                Map.of());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException exception) {
        return errorResponse(
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_REGISTERED",
                exception.getMessage(),
                Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
        return errorResponse(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                Map.of());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(
            RateLimitExceededException exception,
            HttpServletRequest request) {
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String remoteAddr = request != null ? request.getRemoteAddr() : "UNKNOWN";
        LOGGER.warn(
                "Rate limit exceeded for {} {} from remote address: {}, retry-after: {}s",
                method,
                uri,
                remoteAddr,
                exception.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.getRetryAfterSeconds()))
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "RATE_LIMIT_EXCEEDED",
                        "Too many requests",
                        Map.of()));
    }

    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException exception) {
        return handleRateLimitExceeded(exception, null);
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ApiError> handleRequestRejected(
            RequestRejectedException exception,
            HttpServletRequest request) {
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        LOGGER.warn(
                "Request rejected due to invalid request format for {} {}: {}",
                method,
                uri,
                exception.getClass().getSimpleName());
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "The request was rejected due to malformed or disallowed input",
                Map.of());
    }

    public ResponseEntity<ApiError> handleRequestRejected(RequestRejectedException exception) {
        return handleRequestRejected(exception, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        LOGGER.error(
                "Unexpected error while processing request {} {}: {}",
                method,
                uri,
                exception.getClass().getName());
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                Map.of());
    }

    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return handleUnexpected(exception, null);
    }

    private ResponseEntity<ApiError> errorResponse(
            HttpStatus status,
            String code,
            String message,
            Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                fieldErrors));
    }
}
