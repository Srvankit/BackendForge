package com.thecreatorco.backendforge.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "backendforge.production")
public record ProductionConfigurationProperties(
        @NotBlank String dbUrl,
        @NotBlank String dbUsername,
        @NotBlank String dbPassword,
        @NotBlank String jwtSecret,
        @NotBlank String jwtIssuer,
        @NotBlank String jwtAudience,
        @Min(1) long jwtExpirationSeconds) {
}
