package com.thecreatorco.backendforge.ratelimit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

import com.thecreatorco.backendforge.common.error.GlobalExceptionHandler;
import com.thecreatorco.backendforge.user.LoginResponse;
import com.thecreatorco.backendforge.user.UserController;
import com.thecreatorco.backendforge.user.UserLoginRequest;
import com.thecreatorco.backendforge.user.UserRegistrationRequest;
import com.thecreatorco.backendforge.user.UserResponse;
import com.thecreatorco.backendforge.user.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RateLimitInterceptorTest {

    @Test
    void returnsHttp429AfterTheConfiguredLimit() throws Exception {
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        10,
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1))),
                java.time.Clock.systemUTC());
        UserService userService = mock(UserService.class);
        when(userService.login(any(UserLoginRequest.class)))
                .thenReturn(new LoginResponse("token", "Bearer", 900));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new RateLimitInterceptor(limiter))
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("RATE_LIMIT_EXCEEDED")))
                .andExpect(header().string("Retry-After", is("60")));

        verify(userService, times(1)).login(any(UserLoginRequest.class));
    }

    @Test
    void returnsHttp429AfterRegistrationEndpointLimit() throws Exception {
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        10,
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1))),
                java.time.Clock.systemUTC());
        UserService userService = mock(UserService.class);
        when(userService.register(any(UserRegistrationRequest.class)))
                .thenReturn(new UserResponse(UUID.randomUUID(), "user@example.com", true, Instant.now()));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new RateLimitInterceptor(limiter))
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("RATE_LIMIT_EXCEEDED")))
                .andExpect(header().string("Retry-After", is("60")));

        verify(userService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void ignoresForwardedHeadersAndUsesTheRemoteAddress() throws Exception {
        ClientRateLimiter limiter = new ClientRateLimiter(
                new RateLimitProperties(
                        10,
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1)),
                        new RateLimitProperties.Endpoint(1, Duration.ofMinutes(1))),
                java.time.Clock.systemUTC());
        RateLimitInterceptor interceptor = new RateLimitInterceptor(limiter);
        HttpServletResponse response = mock(HttpServletResponse.class);

        MockHttpServletRequest first = request("10.0.0.1", "198.51.100.1");
        interceptor.preHandle(first, response, new Object());

        MockHttpServletRequest spoofed = request("10.0.0.1", "203.0.113.1");
        assertThatThrownBy(() -> interceptor.preHandle(spoofed, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class);
    }

    private MockHttpServletRequest request(String remoteAddress, String forwardedAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr(remoteAddress);
        request.addHeader("X-Forwarded-For", forwardedAddress);
        return request;
    }
}
