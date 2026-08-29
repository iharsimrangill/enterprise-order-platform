package com.portfolio.orders.persistence;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import com.portfolio.orders.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(OrderPersistenceAdapter.class)
class OrderPersistenceAdapterTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndRehydratesOrderAggregate() {
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID customerId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Instant createdAt = Instant.parse("2026-08-29T15:30:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);

        Order order = Order.restore(
                orderId,
                customerId,
                List.of(
                        new OrderLine("SKU-100", 2, new BigDecimal("12.50")),
                        new OrderLine("SKU-200", 1, new BigDecimal("9.99"))),
                OrderStatus.CONFIRMED,
                createdAt,
                updatedAt,
                null);

        orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();

        assertThat(reloaded.id()).isEqualTo(orderId);
        assertThat(reloaded.customerId()).isEqualTo(customerId);
        assertThat(reloaded.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reloaded.createdAt()).isEqualTo(createdAt);
        assertThat(reloaded.updatedAt()).isEqualTo(updatedAt);
        assertThat(reloaded.lines()).hasSize(2);
        assertThat(reloaded.lines().getFirst().sku()).isEqualTo("SKU-100");
        assertThat(reloaded.lines().getLast().sku()).isEqualTo("SKU-200");
        assertThat(reloaded.totalAmount()).isEqualByComparingTo("34.99");
    }

    @Test
    void persistsRejectedOrderReason() {
        UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        Instant now = Instant.parse("2026-08-29T15:30:00Z");
        Order rejected = Order.restore(
                orderId,
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                List.of(new OrderLine("SKU-300", 1, new BigDecimal("5.00"))),
                OrderStatus.REJECTED,
                now,
                now.plusSeconds(30),
                "inventory unavailable");

        orderRepository.save(rejected);
        entityManager.flush();
        entityManager.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(reloaded.rejectionReason()).isEqualTo("inventory unavailable");
    }
}
