package com.portfolio.orders.application;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-29T15:30:00Z");

    @Test
    void createsAndPersistsPendingOrderFromCommand() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        CreateOrderService service = new CreateOrderService(
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC));
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CreateOrderCommand command = new CreateOrderCommand(
                customerId,
                List.of(
                        new CreateOrderCommand.Line("SKU-100", 2, new BigDecimal("12.50")),
                        new CreateOrderCommand.Line("SKU-200", 1, new BigDecimal("9.99"))));

        Order order = service.create(command);

        assertThat(order.id()).isNotNull();
        assertThat(order.customerId()).isEqualTo(customerId);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.createdAt()).isEqualTo(NOW);
        assertThat(order.updatedAt()).isEqualTo(NOW);
        assertThat(order.lines()).hasSize(2);
        assertThat(order.totalAmount()).isEqualByComparingTo("34.99");
        assertThat(repository.savedOrder).isSameAs(order);
    }

    private static final class InMemoryOrderRepository implements OrderRepository {

        private Order savedOrder;

        @Override
        public Order save(Order order) {
            this.savedOrder = order;
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.ofNullable(savedOrder)
                    .filter(order -> order.id().equals(id));
        }
    }
}
