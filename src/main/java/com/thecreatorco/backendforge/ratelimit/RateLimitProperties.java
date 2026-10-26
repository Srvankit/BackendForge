package com.thecreatorco.backendforge.ratelimit;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "backendforge.rate-limit")
public record RateLimitProperties(
        @Min(1) int maxEntries,
        @Valid @NotNull Endpoint login,
        @Valid @NotNull Endpoint register) {

    public record Endpoint(
            @Min(1) int maxRequests,
            @NotNull @DurationMin(nanos = 1) Duration window) {
    }
}
