package com.thecreatorco.backendforge.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RateLimitPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "backendforge.rate-limit.max-entries=100",
                    "backendforge.rate-limit.login.max-requests=10",
                    "backendforge.rate-limit.login.window=60s",
                    "backendforge.rate-limit.register.max-requests=5",
                    "backendforge.rate-limit.register.window=30s");

    @Test
    void acceptsValidRateLimitConfiguration() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(RateLimitProperties.class);
            RateLimitProperties properties = context.getBean(RateLimitProperties.class);
            assertThat(properties.maxEntries()).isEqualTo(100);
            assertThat(properties.login().maxRequests()).isEqualTo(10);
            assertThat(properties.register().maxRequests()).isEqualTo(5);
        });
    }

    @Test
    void rejectsNonPositiveMaxRequests() {
        contextRunner
                .withPropertyValues("backendforge.rate-limit.login.max-requests=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsZeroWindowDuration() {
        contextRunner
                .withPropertyValues("backendforge.rate-limit.register.window=0s")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsNonPositiveMaxEntries() {
        contextRunner
                .withPropertyValues("backendforge.rate-limit.max-entries=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RateLimitProperties.class)
    static class TestConfiguration {
    }
}
