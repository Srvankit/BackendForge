package com.thecreatorco.backendforge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ProductionConfigurationPropertiesTest {

    private static final String VALID_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ProductionConfiguration.class)
            .withPropertyValues(
                    "spring.profiles.active=prod",
                    "backendforge.production.db-url=jdbc:postgresql://db:5432/backendforge",
                    "backendforge.production.db-username=production-user",
                    "backendforge.production.db-password=production-password",
                    "backendforge.production.jwt-secret=" + VALID_SECRET,
                    "backendforge.production.jwt-issuer=backendforge-production",
                    "backendforge.production.jwt-audience=backendforge-api",
                    "backendforge.production.jwt-expiration-seconds=900");

    @Test
    void acceptsCompleteProductionConfiguration() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ProductionConfigurationProperties.class);
        });
    }

    @Test
    void rejectsMissingDatabaseConfiguration() {
        contextRunner
                .withPropertyValues(
                        "backendforge.production.db-url=",
                        "backendforge.production.db-username=",
                        "backendforge.production.db-password=")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsMissingJwtConfiguration() {
        contextRunner
                .withPropertyValues(
                        "backendforge.production.jwt-secret=",
                        "backendforge.production.jwt-issuer=",
                        "backendforge.production.jwt-audience=",
                        "backendforge.production.jwt-expiration-seconds=0")
                .run(context -> assertThat(context).hasFailed());
    }
}
