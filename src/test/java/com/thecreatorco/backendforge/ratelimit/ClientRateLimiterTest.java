package com.thecreatorco.backendforge.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ClientRateLimiterTest {

    private static final RateLimitProperties PROPERTIES = new RateLimitProperties(
            2,
            new RateLimitProperties.Endpoint(2, Duration.ofSeconds(10)),
            new RateLimitProperties.Endpoint(1, Duration.ofSeconds(20)));

    @Test
    void rejectsTheRequestAfterTheEndpointLimit() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(PROPERTIES, clock);

        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.1");

        Throwable thrown = catchThrowable(() -> limiter.check("login", "10.0.0.1"));

        assertThat(thrown)
                .isInstanceOf(RateLimitExceededException.class);
        assertThat(((RateLimitExceededException) thrown).getRetryAfterSeconds())
                .isEqualTo(10L);
    }

    @Test
    void keepsLoginAndRegistrationQuotasIndependent() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(PROPERTIES, clock);

        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.1");
        limiter.check("register", "10.0.0.1");

        assertThatThrownBy(() -> limiter.check("register", "10.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void resetsTheQuotaAfterTheWindowExpires() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(PROPERTIES, clock);

        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.1");
        clock.advance(Duration.ofSeconds(10));

        limiter.check("login", "10.0.0.1");
    }

    @Test
    void keepsDifferentClientAddressesIndependent() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(PROPERTIES, clock);

        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.2");
    }

    @Test
    void boundsStoredClientWindowsWithoutEvictingActiveQuotas() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        2,
                        new RateLimitProperties.Endpoint(2, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(2, Duration.ofMinutes(1))),
                clock);

        limiter.check("login", "10.0.0.1");
        limiter.check("login", "10.0.0.2");

        assertThatThrownBy(() -> limiter.check("login", "10.0.0.3"))
                .isInstanceOf(RateLimitExceededException.class);
        assertThat(limiter.entryCount()).isEqualTo(2);
    }

    @Test
    void doesNotResetExhaustedQuotaWhenMapIsFull() {
        MutableClock clock = new MutableClock();
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        2,
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1))),
                clock);

        limiter.check("login", "10.0.0.1");
        assertThatThrownBy(() -> limiter.check("login", "10.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);

        limiter.check("login", "10.0.0.2");
        assertThatThrownBy(() -> limiter.check("login", "10.0.0.3"))
                .isInstanceOf(RateLimitExceededException.class);
        assertThatThrownBy(() -> limiter.check("login", "10.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void enforcesLimitsUnderConcurrentAccess() throws Exception {
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        100,
                        new RateLimitProperties.Endpoint(50, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(50, Duration.ofMinutes(1))),
                Clock.systemUTC());

        int attempts = 80;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            futures.add(pool.submit(() -> {
                try {
                    limiter.check("login", "10.0.0.1");
                    allowed.incrementAndGet();
                } catch (RateLimitExceededException exception) {
                    rejected.incrementAndGet();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        assertThat(allowed.get()).isEqualTo(50);
        assertThat(rejected.get()).isEqualTo(30);
    }

    private static final class MutableClock extends Clock {
        private Instant current = Instant.parse("2026-01-01T00:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }

        void advance(Duration duration) {
            current = current.plus(duration);
        }
    }
}
