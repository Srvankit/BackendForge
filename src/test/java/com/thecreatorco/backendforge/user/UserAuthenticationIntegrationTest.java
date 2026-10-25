package com.thecreatorco.backendforge.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecreatorco.backendforge.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserAuthenticationIntegrationTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "correct-horse-battery";
    private static final String JWT_ISSUER = "backendforge";
    private static final String JWT_AUDIENCE = "backendforge-api";
    private static final String JWT_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
        userRepository.flush();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> JWT_SECRET);
    }

    @Test
    void logsInWithCorrectCredentialsAndReturnsValidJwt() throws Exception {
        register();

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();
        assertThat(jwtService.parseAndValidate(token).getSubject()).isNotBlank();
        assertThat(jwtService.parseAndValidate(token).get("email", String.class))
                .isEqualTo(EMAIL);
        assertThat(jwtService.parseAndValidate(token).getIssuer()).isEqualTo(JWT_ISSUER);
        assertThat(jwtService.parseAndValidate(token).getAudience())
                .containsExactly(JWT_AUDIENCE);
        assertThat(jwtService.parseAndValidate(token).get("token_use", String.class))
                .isEqualTo("access");
    }

    @Test
    void rejectsIncorrectPasswordAndUnknownEmailWithSameError() throws Exception {
        register();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(EMAIL, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("unknown@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void rejectsLoginForDisabledAccountWithGenericSafeError() throws Exception {
        createUser(false);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(EMAIL, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .doesNotContain(PASSWORD)
                .doesNotContain("passwordHash")
                .doesNotContain("JWT_SECRET")
                .doesNotContain("stack trace");
    }

    @Test
    void rejectsMalformedExpiredAndIncorrectlySignedTokens() throws Exception {
        register();
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        String otherSecret = Base64.getEncoder().encodeToString(
                "another-test-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));

        assertUnauthorized("not-a-jwt");

        String expired = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", EMAIL)
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET)),
                        Jwts.SIG.HS256)
                .compact();
        assertUnauthorized(expired);

        String incorrectlySigned = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", EMAIL)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(otherSecret)),
                        Jwts.SIG.HS256)
                .compact();
        assertUnauthorized(incorrectlySigned);
    }

    @Test
    void rejectsTokensWithMissingOrIncorrectRequiredClaims() throws Exception {
        register();
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();

        assertUnauthorized(tokenWithClaims(user, "wrong-issuer", JWT_AUDIENCE, "access"));
        assertUnauthorized(tokenWithClaims(user, JWT_ISSUER, "wrong-audience", "access"));
        assertUnauthorized(tokenWithClaims(user, null, JWT_AUDIENCE, "access"));
        assertUnauthorized(tokenWithClaims(user, JWT_ISSUER, null, "access"));
        assertUnauthorized(tokenWithClaims(user, JWT_ISSUER, JWT_AUDIENCE, null));
        assertUnauthorized(tokenWithClaims(user, JWT_ISSUER, JWT_AUDIENCE, "refresh"));
    }

    @Test
    void permitsOpenApiDocumentationAndRequiresAuthenticationForOtherProtectedEndpoints() throws Exception {
        register();
        String token = loginToken();

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permitsSwaggerUiAndYamlDocumentationWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exposesReadinessHealthWithoutAuthenticationAndWithoutDetails() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void doesNotExposeOtherActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsPreviouslyIssuedTokenAfterAccountIsDisabled() throws Exception {
        register();
        String token = loginToken();
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();

        disableUser(user);

        mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void rejectsTokenWhenEmailClaimDoesNotMatchCurrentUser() throws Exception {
        register();
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();

        assertUnauthorized(tokenWithClaims(user, "other@example.com",
                JWT_ISSUER, JWT_AUDIENCE, "access"));
    }

    @Test
    void rejectsPreviouslyIssuedTokenAfterEmailChanges() throws Exception {
        register();
        String token = loginToken();
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        Instant now = Instant.now();
        userRepository.saveAndFlush(new UserEntity(
                user.getId(),
                "updated@example.com",
                user.getPasswordHash(),
                true,
                user.getCreatedAt(),
                now));

        assertUnauthorized(token);
    }

    @Test
    void keepsRegistrationPublicAndRejectsInvalidLoginInput() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"public@example.com","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsMalformedLoginJsonWithSafeApiError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message").value("Request body could not be parsed"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("stack"))));
    }

    @Test
    void rejectsUnknownLoginJsonPropertyWithSafeApiError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"correct-horse-battery",
                                "unexpected":"value"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("UNKNOWN_JSON_PROPERTY"))
                .andExpect(jsonPath("$.message").value("Request contains an unknown property"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("unexpected"))))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("stack"))));
    }

    @Test
    void rejectsUnsupportedLoginContentTypeWithSafeApiError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message").value(
                        "Request content type is not supported"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private void register() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"user@example.com","password":"correct-horse-battery"}
                        """))
                .andExpect(status().isCreated());
    }

    private void createUser(boolean enabled) {
        Instant now = Instant.now();
        userRepository.saveAndFlush(new UserEntity(
                UUID.randomUUID(),
                EMAIL,
                passwordEncoder.encode(PASSWORD),
                enabled,
                now,
                now));
    }

    private void disableUser(UserEntity user) {
        Instant now = Instant.now();
        userRepository.saveAndFlush(new UserEntity(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                false,
                user.getCreatedAt(),
                now));
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("accessToken").asText();
    }

    private void assertUnauthorized(String token) throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String tokenWithClaims(
            UserEntity user,
            String issuer,
            String audience,
            String tokenUse) {
        return tokenWithClaims(user, user.getEmail(), issuer, audience, tokenUse);
    }

    private String tokenWithClaims(
            UserEntity user,
            String email,
            String issuer,
            String audience,
            String tokenUse) {
        var builder = Jwts.builder()
                        .subject(user.getId().toString())
                        .claim("email", email)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 60_000));
        if (issuer != null) {
            builder.issuer(issuer);
        }
        if (audience != null) {
            builder.audience().add(audience).and();
        }
        if (tokenUse != null) {
            builder.claim("token_use", tokenUse);
        }
        return builder.signWith(
                                Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET)),
                                Jwts.SIG.HS256)
                        .compact();
    }

    private String loginJson(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }
}
