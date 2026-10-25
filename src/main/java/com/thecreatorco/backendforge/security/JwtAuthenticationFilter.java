package com.thecreatorco.backendforge.security;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecreatorco.backendforge.common.error.ApiError;
import com.thecreatorco.backendforge.user.UserEntity;
import com.thecreatorco.backendforge.user.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            writeUnauthorized(response);
            return;
        }

        try {
            Claims claims = jwtService.parseAndValidate(token);
            UUID userId = UUID.fromString(claims.getSubject());
            UserEntity user = userRepository.findById(userId)
                    .filter(UserEntity::isEnabled)
                    .orElseThrow(() -> new JwtException("User is not available"));
            String tokenEmail = claims.get("email", String.class);
            if (!user.getEmail().equals(tokenEmail)) {
                throw new JwtException("Token identity does not match the current user");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, userAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response);
        }
    }

    private java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority>
            userAuthorities() {
        return java.util.List.of();
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ApiError(
                java.time.Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Authentication is required",
                java.util.Map.of()));
    }
}
