package com.portfolio.inventory.persistence;

import com.portfolio.inventory.application.ReservationProcessingResult;
import com.portfolio.inventory.application.ReserveInventoryService;
import com.portfolio.inventory.application.port.InventoryReservationRepository;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class InventoryPersistenceIntegrationTest {
    @Autowired
    private ReserveInventoryService reserveInventoryService;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void seedStock() {
        jdbcTemplate.update("DELETE FROM processed_event");
        jdbcTemplate.update("DELETE FROM inventory_reservation_line");
        jdbcTemplate.update("DELETE FROM inventory_reservation");
        jdbcTemplate.update("DELETE FROM inventory_stock");
        jdbcTemplate.update(
                "INSERT INTO inventory_stock (sku, available_quantity, reserved_quantity, version, updated_at) VALUES (?, ?, 0, 0, CURRENT_TIMESTAMP)",
                "SKU-DB-1", 10);
        jdbcTemplate.update(
                "INSERT INTO inventory_stock (sku, available_quantity, reserved_quantity, version, updated_at) VALUES (?, ?, 0, 0, CURRENT_TIMESTAMP)",
                "SKU-DB-2", 2);
    }

    @Test
    void persistsReservedInventoryAndProcessedEventAtomically() {
        var event = event("SKU-DB-1", 3, "SKU-DB-2", 1);

        var result = reserveInventoryService.handle(event);

        assertThat(result.status()).isEqualTo(ReservationProcessingResult.Status.PROCESSED);
        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservationRepository.findByOrderId(event.orderId())).isPresent();
        assertThat(quantity("SKU-DB-1", "available_quantity")).isEqualTo(7);
        assertThat(quantity("SKU-DB-1", "reserved_quantity")).isEqualTo(3);
        assertThat(processedEventCount(event.eventId())).isEqualTo(1);
    }

    @Test
    void persistsRejectedReservationWithoutReducingStock() {
        var event = event("SKU-DB-1", 3, "SKU-DB-2", 9);

        var result = reserveInventoryService.handle(event);

        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(result.reservation().reason()).contains("SKU-DB-2");
        assertThat(quantity("SKU-DB-1", "available_quantity")).isEqualTo(10);
        assertThat(quantity("SKU-DB-2", "available_quantity")).isEqualTo(2);
        assertThat(processedEventCount(event.eventId())).isEqualTo(1);
    }

    @Test
    void returnsDuplicateForAlreadyProcessedEvent() {
        var event = event("SKU-DB-1", 1, "SKU-DB-2", 1);

        reserveInventoryService.handle(event);
        var duplicate = reserveInventoryService.handle(event);

        assertThat(duplicate.status()).isEqualTo(ReservationProcessingResult.Status.DUPLICATE);
        assertThat(quantity("SKU-DB-1", "available_quantity")).isEqualTo(9);
        assertThat(quantity("SKU-DB-2", "available_quantity")).isEqualTo(1);
    }

    private int quantity(String sku, String column) {
        return jdbcTemplate.queryForObject(
                "SELECT " + column + " FROM inventory_stock WHERE sku = ?",
                Integer.class,
                sku);
    }

    private int processedEventCount(UUID eventId) {
        entityManager.flush();
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM processed_event WHERE event_id = ?",
                Integer.class,
                eventId);
    }

    private static OrderCreatedEvent event(String sku1, int qty1, String sku2, int qty2) {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                "order.created",
                1,
                Instant.parse("2026-08-29T16:00:00Z"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PENDING",
                BigDecimal.valueOf(50),
                List.of(
                        new OrderCreatedEvent.Line(sku1, qty1, BigDecimal.TEN, BigDecimal.valueOf(20)),
                        new OrderCreatedEvent.Line(sku2, qty2, BigDecimal.valueOf(30), BigDecimal.valueOf(30))));
    }
}
