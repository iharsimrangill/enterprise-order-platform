package com.portfolio.orders.application;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.application.port.ProcessedInventoryEventRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import com.portfolio.orders.domain.OrderStatus;
import com.portfolio.orders.messaging.event.InventoryOutcomeEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandleInventoryOutcomeServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID EVENT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final Instant CREATED_AT = Instant.parse("2026-08-29T16:00:00Z");
    private static final Instant OUTCOME_AT = CREATED_AT.plusSeconds(30);
    private static final Instant PROCESSED_AT = CREATED_AT.plusSeconds(45);

    @Test
    void confirmsPendingOrderWhenInventoryIsReserved() {
        InMemoryOrderRepository orders = new InMemoryOrderRepository(pendingOrder());
        InMemoryProcessedEventRepository processed = new InMemoryProcessedEventRepository();
        HandleInventoryOutcomeService service = service(orders, processed);

        InventoryOutcomeHandlingResult result = service.handle(reservedEvent(EVENT_ID));

        assertThat(result).isEqualTo(InventoryOutcomeHandlingResult.APPLIED);
        assertThat(orders.order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(orders.order.updatedAt()).isEqualTo(OUTCOME_AT);
        assertThat(processed.eventIds).containsExactly(EVENT_ID);
    }

    @Test
    void rejectsPendingOrderWhenInventoryIsRejected() {
        InMemoryOrderRepository orders = new InMemoryOrderRepository(pendingOrder());
        InMemoryProcessedEventRepository processed = new InMemoryProcessedEventRepository();
        HandleInventoryOutcomeService service = service(orders, processed);

        InventoryOutcomeHandlingResult result = service.handle(rejectedEvent(EVENT_ID));

        assertThat(result).isEqualTo(InventoryOutcomeHandlingResult.APPLIED);
        assertThat(orders.order.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(orders.order.rejectionReason()).isEqualTo("insufficient stock for SKU-100");
        assertThat(processed.eventIds).containsExactly(EVENT_ID);
    }

    @Test
    void ignoresAlreadyProcessedEvent() {
        InMemoryOrderRepository orders = new InMemoryOrderRepository(pendingOrder());
        InMemoryProcessedEventRepository processed = new InMemoryProcessedEventRepository();
        processed.eventIds.add(EVENT_ID);
        HandleInventoryOutcomeService service = service(orders, processed);

        InventoryOutcomeHandlingResult result = service.handle(reservedEvent(EVENT_ID));

        assertThat(result).isEqualTo(InventoryOutcomeHandlingResult.DUPLICATE);
        assertThat(orders.order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(orders.saveCount).isZero();
    }

    @Test
    void rejectsContradictoryOutcomeForTerminalOrder() {
        Order order = pendingOrder();
        order.reject("no stock", OUTCOME_AT);
        InMemoryOrderRepository orders = new InMemoryOrderRepository(order);
        HandleInventoryOutcomeService service = service(orders, new InMemoryProcessedEventRepository());

        assertThatThrownBy(() -> service.handle(reservedEvent(EVENT_ID)))
                .isInstanceOf(InventoryOutcomeConflictException.class)
                .hasMessageContaining("inventory.reserved")
                .hasMessageContaining("REJECTED");
    }

    private static HandleInventoryOutcomeService service(
            InMemoryOrderRepository orders,
            InMemoryProcessedEventRepository processed) {
        return new HandleInventoryOutcomeService(
                orders,
                processed,
                Clock.fixed(PROCESSED_AT, ZoneOffset.UTC));
    }

    private static Order pendingOrder() {
        return Order.place(
                ORDER_ID,
                CUSTOMER_ID,
                List.of(new OrderLine("SKU-100", 2, new BigDecimal("10.00"))),
                CREATED_AT);
    }

    private static InventoryOutcomeEvent reservedEvent(UUID eventId) {
        return new InventoryOutcomeEvent(
                eventId,
                "inventory.reserved",
                1,
                OUTCOME_AT,
                ORDER_ID,
                UUID.fromString("40000000-0000-0000-0000-000000000004"),
                null,
                List.of(new InventoryOutcomeEvent.Line("SKU-100", 2)));
    }

    private static InventoryOutcomeEvent rejectedEvent(UUID eventId) {
        return new InventoryOutcomeEvent(
                eventId,
                "inventory.rejected",
                1,
                OUTCOME_AT,
                ORDER_ID,
                UUID.fromString("40000000-0000-0000-0000-000000000004"),
                "insufficient stock for SKU-100",
                List.of(new InventoryOutcomeEvent.Line("SKU-100", 2)));
    }

    private static final class InMemoryOrderRepository implements OrderRepository {
        private Order order;
        private int saveCount;

        private InMemoryOrderRepository(Order order) {
            this.order = order;
        }

        @Override
        public Order save(Order order) {
            this.order = order;
            saveCount++;
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return order.id().equals(id) ? Optional.of(order) : Optional.empty();
        }
    }

    private static final class InMemoryProcessedEventRepository implements ProcessedInventoryEventRepository {
        private final Set<UUID> eventIds = new HashSet<>();

        @Override
        public boolean existsByEventId(UUID eventId) {
            return eventIds.contains(eventId);
        }

        @Override
        public void markProcessed(UUID eventId, UUID orderId, String eventType, Instant processedAt) {
            eventIds.add(eventId);
        }
    }
}
