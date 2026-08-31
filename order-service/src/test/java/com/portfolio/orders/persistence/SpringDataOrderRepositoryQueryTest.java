package com.portfolio.orders.persistence;

import com.portfolio.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SpringDataOrderRepositoryQueryTest {

    @Autowired
    private SpringDataOrderRepository repository;

    @Test
    void shouldReturnRecentOrdersForCustomerOrderedByCreatedAtDescending() {
        UUID customerId = UUID.randomUUID();

        repository.save(new OrderEntity(
                UUID.randomUUID(),
                customerId,
                OrderStatus.PENDING,
                Instant.parse("2026-08-30T10:00:00Z"),
                Instant.parse("2026-08-30T10:00:00Z"),
                null));

        repository.save(new OrderEntity(
                UUID.randomUUID(),
                customerId,
                OrderStatus.CONFIRMED,
                Instant.parse("2026-08-31T10:00:00Z"),
                Instant.parse("2026-08-31T10:00:00Z"),
                null));

        List<OrderEntity> results =
                repository.findByCustomerIdOrderByCreatedAtDesc(
                        customerId,
                        PageRequest.of(0, 10));

        assertEquals(2, results.size());
        assertEquals(
                Instant.parse("2026-08-31T10:00:00Z"),
                results.get(0).createdAt());
    }

    @Test
    void shouldReturnRecentOrdersForStatusOrderedByCreatedAtDescending() {
        repository.save(new OrderEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderStatus.CONFIRMED,
                Instant.parse("2026-08-29T10:00:00Z"),
                Instant.parse("2026-08-29T10:00:00Z"),
                null));

        repository.save(new OrderEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderStatus.CONFIRMED,
                Instant.parse("2026-08-31T11:00:00Z"),
                Instant.parse("2026-08-31T11:00:00Z"),
                null));

        List<OrderEntity> results =
                repository.findByStatusOrderByCreatedAtDesc(
                        OrderStatus.CONFIRMED,
                        PageRequest.of(0, 10));

        assertEquals(2, results.size());
        assertEquals(
                Instant.parse("2026-08-31T11:00:00Z"),
                results.get(0).createdAt());
    }
}
