package com.shoploc.order.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Flyway migrations.
 * Verifies schema creation, constraints, and business rules enforcement.
 */
@SpringBootTest
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_order_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testAllTablesCreated() {
        // Verify all 6 tables exist
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name"
        );

        List<String> tableNames = tables.stream()
            .map(m -> (String) m.get("table_name"))
            .toList();

        assertThat(tableNames)
            .contains("cart", "cart_item", "order_checkout", "customer_order", "order_line", "order_status_history");
    }

    @Test
    void testCartTableStructure() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT column_name, data_type, is_nullable FROM information_schema.columns " +
            "WHERE table_name = 'cart' ORDER BY ordinal_position"
        );

        assertThat(columns).isNotEmpty().hasSize(4);
        List<String> columnNames = columns.stream()
            .map(m -> (String) m.get("column_name"))
            .toList();
        assertThat(columnNames).contains("id", "customer_id", "created_at", "updated_at");
    }

    @Test
    void testStatusConstraints() {
        // Create test checkout
        jdbcTemplate.update(
            "INSERT INTO order_checkout (id, customer_id, request_hash, total_amount) " +
            "VALUES ('550e8400-e29b-41d4-a716-446655440000'::uuid, '660e8400-e29b-41d4-a716-446655440001'::uuid, 'hash123', 5000)"
        );

        // Invalid status should be rejected
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO customer_order (id, checkout_id, customer_id, store_id, store_name, status, " +
                "payment_mode, payment_status, pickup_slot_start, pickup_slot_end, total_amount) " +
                "VALUES ('760e8400-e29b-41d4-a716-446655440002'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, " +
                "'660e8400-e29b-41d4-a716-446655440001'::uuid, '860e8400-e29b-41d4-a716-446655440003'::uuid, 'Store 1', " +
                "'INVALID_STATUS', 'ON_SITE', 'TO_PAY', NOW(), NOW() + INTERVAL '30 minutes', 5000)"
            )
        ).isNotNull();

        // Valid status should be accepted
        jdbcTemplate.update(
            "INSERT INTO customer_order (id, checkout_id, customer_id, store_id, store_name, status, " +
            "payment_mode, payment_status, pickup_slot_start, pickup_slot_end, total_amount) " +
            "VALUES ('760e8400-e29b-41d4-a716-446655440002'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, " +
            "'660e8400-e29b-41d4-a716-446655440001'::uuid, '860e8400-e29b-41d4-a716-446655440003'::uuid, 'Store 1', " +
            "'CONFIRMED', 'ON_SITE', 'TO_PAY', NOW(), NOW() + INTERVAL '30 minutes', 5000)"
        );

        List<Map<String, Object>> orders = jdbcTemplate.queryForList(
            "SELECT COUNT(*) as count FROM customer_order WHERE status = 'CONFIRMED'"
        );
        assertThat(orders.get(0).get("count")).isEqualTo(1L);
    }

    @Test
    void testIndexesExist() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            "SELECT indexname FROM pg_indexes WHERE tablename IN ('customer_order', 'order_line')"
        );

        List<String> indexNames = indexes.stream()
            .map(m -> (String) m.get("indexname"))
            .toList();

        assertThat(indexNames)
            .contains("idx_order_customer_created")
            .contains("idx_order_line_order_id");
    }
}
