package com.thecreatorco.backendforge.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    private static final String JWT_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> JWT_SECRET);
    }

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void savesAndRetrievesUserById() {
        UserEntity saved = userRepository.saveAndFlush(newUser("save@example.com"));

        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(UserEntity::getEmail)
                .isEqualTo("save@example.com");
    }

    @Test
    void findsUserByEmail() {
        UserEntity saved = userRepository.saveAndFlush(newUser("find@example.com"));

        assertThat(userRepository.findByEmail(saved.getEmail()))
                .isPresent()
                .get()
                .extracting(UserEntity::getId)
                .isEqualTo(saved.getId());
    }

    @Test
    void checksWhetherEmailExists() {
        userRepository.saveAndFlush(newUser("exists@example.com"));

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void updatesPersistedUser() {
        UserEntity saved = userRepository.saveAndFlush(newUser("update@example.com"));
        saved.setPasswordHash("updated-hash");
        saved.setUpdatedAt(Instant.now());

        userRepository.saveAndFlush(saved);

        entityManager.clear();
        UserEntity reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPasswordHash()).isEqualTo("updated-hash");
    }

    @Test
    void deletesUser() {
        UserEntity saved = userRepository.saveAndFlush(newUser("delete@example.com"));

        userRepository.delete(saved);
        userRepository.flush();

        assertThat(userRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(newUser("duplicate@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("duplicate@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserEntity newUser(String email) {
        Instant now = Instant.now();
        return new UserEntity(UUID.randomUUID(), email, "hashed-password", true, now, now);
    }
}
