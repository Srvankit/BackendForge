package com.thecreatorco.backendforge.user;

import java.net.URI;

import jakarta.validation.Valid;

import com.thecreatorco.backendforge.common.error.ApiError;
import com.thecreatorco.backendforge.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Users", description = "User registration, authentication, and profile endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user account with an email and password.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error or malformed request",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email already registered",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Rate limit exceeded",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            })
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.register(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns a JWT access token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error or malformed request",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Rate limit exceeded",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            })
    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @Operation(
            summary = "Get current authenticated user",
            description = "Retrieves profile details for the authenticated user represented by the JWT access token.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication is required or token is invalid",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            })
    @GetMapping("/users/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserEntity user) {
        return userService.getCurrentUser(user);
    }
}
