package com.thecreatorco.backendforge;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DatabaseMigrationIntegrationTest {

    private static final String JWT_SECRET =
            "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IHRoYXQgaXMgYXQgbGVhc3QgMzI=";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> JWT_SECRET);
    }

    @Test
    void appliesMigrationAndCreatesExpectedSchema() throws SQLException {
        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .contains("1");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'users'",
                Integer.class))
                .isEqualTo(1);

        Map<String, Integer> columns = jdbcTemplate.query(
                "SELECT column_name, ordinal_position "
                        + "FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'users'",
                (ResultSet resultSet) -> {
                    Map<String, Integer> result = new java.util.HashMap<>();
                    while (resultSet.next()) {
                        result.put(resultSet.getString("column_name"),
                                resultSet.getInt("ordinal_position"));
                    }
                    return result;
                });

        assertThat(columns).containsKeys(
                "id", "email", "password_hash", "enabled", "created_at", "updated_at");
        assertThat(columns.get("id")).isEqualTo(1);
        assertThat(columns.get("updated_at")).isEqualTo(6);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes "
                        + "WHERE schemaname = 'public' AND tablename = 'users' "
                        + "AND indexname = 'uq_users_email'",
                Integer.class))
                .isEqualTo(1);

        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)",
                userId, "user@example.com", "hashed-password");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT enabled FROM users WHERE id = ?", Boolean.class, userId))
                .isTrue();
    }

    @Test
    void applicationContextStartsWithHibernateValidationEnabled() {
        assertThat(dataSource).isNotNull();
    }
}
