package com.thecreatorco.backendforge.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRateLimiter.class);

    private final RateLimitProperties properties;
    private final Clock clock;
    private final Map<String, Window> windows = new HashMap<>();

    @Autowired
    public ClientRateLimiter(RateLimitProperties properties) {
        this(properties, Clock.systemUTC());
    }

    ClientRateLimiter(RateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public synchronized void check(String endpoint, String clientKey) {
        Instant now = clock.instant();
        removeExpired(now);

        RateLimitProperties.Endpoint endpointProperties = endpointProperties(endpoint);
        String key = endpoint + ":" + clientKey;
        Window window = windows.get(key);

        if (window == null) {
            if (windows.size() >= properties.maxEntries()) {
                LOGGER.warn("Rate limit table capacity reached ({} entries); rejecting client", properties.maxEntries());
                throw new RateLimitExceededException(retryAfterSeconds(now));
            }
            windows.put(key, new Window(now, 1));
            return;
        }

        Instant windowEnd = window.startedAt.plus(endpointProperties.window());
        if (!now.isBefore(windowEnd)) {
            window.startedAt = now;
            window.requests = 1;
            return;
        }

        if (window.requests >= endpointProperties.maxRequests()) {
            throw new RateLimitExceededException(retryAfterSeconds(now, windowEnd));
        }

        window.requests++;
    }

    private RateLimitProperties.Endpoint endpointProperties(String endpoint) {
        return switch (endpoint) {
            case "login" -> properties.login();
            case "register" -> properties.register();
            default -> throw new IllegalArgumentException("Unknown rate-limit endpoint");
        };
    }

    private void removeExpired(Instant now) {
        Iterator<Map.Entry<String, Window>> iterator = windows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Window> entry = iterator.next();
            String endpoint = entry.getKey().substring(0, entry.getKey().indexOf(':'));
            if (!now.isBefore(entry.getValue().startedAt.plus(
                    endpointProperties(endpoint).window()))) {
                iterator.remove();
            }
        }
    }

    private long retryAfterSeconds(Instant now) {
        long soonestSeconds = Long.MAX_VALUE;
        for (Map.Entry<String, Window> entry : windows.entrySet()) {
            String endpoint = entry.getKey().substring(0, entry.getKey().indexOf(':'));
            Instant windowEnd = entry.getValue().startedAt.plus(
                    endpointProperties(endpoint).window());
            soonestSeconds = Math.min(soonestSeconds, retryAfterSeconds(now, windowEnd));
        }
        return soonestSeconds == Long.MAX_VALUE ? 1L : soonestSeconds;
    }

    private static long retryAfterSeconds(Instant now, Instant windowEnd) {
        return Math.max(
                1L,
                (long) Math.ceil(Duration.between(now, windowEnd).toMillis() / 1000.0));
    }

    int entryCount() {
        synchronized (this) {
            return windows.size();
        }
    }

    static final class Window {
        private Instant startedAt;
        private int requests;

        Window(Instant startedAt, int requests) {
            this.startedAt = startedAt;
            this.requests = requests;
        }
    }
}
