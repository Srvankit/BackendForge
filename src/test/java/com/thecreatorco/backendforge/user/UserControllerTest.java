package com.thecreatorco.backendforge.user;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import com.thecreatorco.backendforge.common.error.GlobalExceptionHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsSafeProfileWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-10-26T20:00:00Z");
        UserEntity user = new UserEntity(userId, "authenticated@example.com", "secret-hash", true, createdAt, createdAt);

        when(userService.getCurrentUser(any(UserEntity.class)))
                .thenReturn(new UserResponse(userId, "authenticated@example.com", true, createdAt));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/v1/users/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.email", is("authenticated@example.com")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.dummyPasswordHash").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void getCurrentUserReturns401WhenUnauthenticated() throws Exception {
        when(userService.getCurrentUser(null))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    void ignoresRequestParametersAndOnlyUsesAuthenticatedPrincipal() throws Exception {
        UUID authenticatedId = UUID.randomUUID();
        Instant now = Instant.now();
        UserEntity user = new UserEntity(authenticatedId, "actual@example.com", "hash", true, now, now);

        when(userService.getCurrentUser(any(UserEntity.class)))
                .thenReturn(new UserResponse(authenticatedId, "actual@example.com", true, now));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/v1/users/me")
                        .param("id", UUID.randomUUID().toString())
                        .param("email", "spoofed@example.com")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(authenticatedId.toString())))
                .andExpect(jsonPath("$.email", is("actual@example.com")));
    }
}
