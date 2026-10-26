package com.thecreatorco.backendforge.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";

    private final ClientRateLimiter rateLimiter;

    public RateLimitInterceptor(ClientRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String endpoint = endpointFor(request.getRequestURI());
        if (endpoint == null) {
            return true;
        }

        String remoteAddress = request.getRemoteAddr();
        rateLimiter.check(endpoint, remoteAddress == null ? "unknown" : remoteAddress);
        return true;
    }

    private String endpointFor(String path) {
        if (LOGIN_PATH.equals(path)) {
            return "login";
        }
        if (REGISTER_PATH.equals(path)) {
            return "register";
        }
        return null;
    }
}
