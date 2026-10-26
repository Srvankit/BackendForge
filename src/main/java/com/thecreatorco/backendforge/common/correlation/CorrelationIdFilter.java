package com.thecreatorco.backendforge.common.correlation;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_KEY = "correlationId";
    public static final String REQUEST_ATTRIBUTE = "correlationId";
    public static final int MAX_CORRELATION_ID_LENGTH = 64;

    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("^[a-zA-Z0-9_.-]{1,64}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);

        MDC.put(MDC_KEY, correlationId);
        request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        if (request.getHeader(REQUEST_ID_HEADER) != null) {
            response.setHeader(REQUEST_ID_HEADER, correlationId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    public static String getCorrelationId() {
        return MDC.get(MDC_KEY);
    }

    public static boolean isValid(String id) {
        if (id == null) {
            return false;
        }
        String trimmed = id.trim();
        return !trimmed.isEmpty()
                && trimmed.length() <= MAX_CORRELATION_ID_LENGTH
                && VALID_CORRELATION_ID.matcher(trimmed).matches();
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader(CORRELATION_ID_HEADER);
        if (isValid(incoming)) {
            return incoming.trim();
        }

        String fallback = request.getHeader(REQUEST_ID_HEADER);
        if (isValid(fallback)) {
            return fallback.trim();
        }

        return UUID.randomUUID().toString();
    }
}
