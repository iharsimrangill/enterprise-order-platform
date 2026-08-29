package com.portfolio.orders.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final Instant PLACED_AT = Instant.parse("2026-08-29T14:00:00Z");

    @Test
    void placesOrderInPendingStateAndCalculatesTotal() {
        Order order = sampleOrder();

        assertEquals(OrderStatus.PENDING, order.status());
        assertEquals(new BigDecimal("45.50"), order.totalAmount());
        assertEquals(PLACED_AT, order.createdAt());
        assertEquals(PLACED_AT, order.updatedAt());
        assertNull(order.rejectionReason());
    }

    @Test
    void confirmsPendingOrder() {
        Order order = sampleOrder();
        Instant confirmedAt = PLACED_AT.plusSeconds(30);

        order.confirm(confirmedAt);

        assertEquals(OrderStatus.CONFIRMED, order.status());
        assertEquals(confirmedAt, order.updatedAt());
    }

    @Test
    void fulfillsConfirmedOrder() {
        Order order = sampleOrder();
        order.confirm(PLACED_AT.plusSeconds(30));

        order.fulfill(PLACED_AT.plusSeconds(60));

        assertEquals(OrderStatus.FULFILLED, order.status());
    }

    @Test
    void rejectsPendingOrderWithReason() {
        Order order = sampleOrder();

        order.reject("Inventory unavailable", PLACED_AT.plusSeconds(30));

        assertEquals(OrderStatus.REJECTED, order.status());
        assertEquals("Inventory unavailable", order.rejectionReason());
    }

    @Test
    void preventsIllegalTransitionFromPendingToFulfilled() {
        Order order = sampleOrder();

        assertThrows(
                InvalidOrderStateTransitionException.class,
                () -> order.fulfill(PLACED_AT.plusSeconds(30)));
    }

    @Test
    void preventsTransitionAfterTerminalState() {
        Order order = sampleOrder();
        order.cancel(PLACED_AT.plusSeconds(30));

        assertThrows(
                InvalidOrderStateTransitionException.class,
                () -> order.confirm(PLACED_AT.plusSeconds(60)));
    }

    @Test
    void rejectsOrderWithoutLines() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Order.place(UUID.randomUUID(), UUID.randomUUID(), List.of(), PLACED_AT));
    }

    @Test
    void preventsTransitionTimeFromMovingBackwards() {
        Order order = sampleOrder();
        order.confirm(PLACED_AT.plusSeconds(30));

        assertThrows(
                IllegalArgumentException.class,
                () -> order.cancel(PLACED_AT.plusSeconds(15)));
    }

    private static Order sampleOrder() {
        return Order.place(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(
                        new OrderLine("SKU-100", 2, new BigDecimal("10.00")),
                        new OrderLine("SKU-200", 1, new BigDecimal("25.50"))),
                PLACED_AT);
    }
}
