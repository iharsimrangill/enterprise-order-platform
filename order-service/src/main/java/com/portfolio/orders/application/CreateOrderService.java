package com.portfolio.orders.application;

import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderOutboxWriter;
import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import com.portfolio.orders.observability.OrderMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final OrderOutboxWriter orderOutboxWriter;
    private final Clock clock;
    private final OrderMetrics orderMetrics;

    @Autowired
    public CreateOrderService(
            OrderRepository orderRepository,
            OrderOutboxWriter orderOutboxWriter,
            OrderMetrics orderMetrics) {
        this(
                orderRepository,
                orderOutboxWriter,
                Clock.systemUTC(),
                orderMetrics);
    }

    CreateOrderService(
            OrderRepository orderRepository,
            OrderOutboxWriter orderOutboxWriter,
            Clock clock) {
        this(orderRepository, orderOutboxWriter, clock, null);
    }

    CreateOrderService(
            OrderRepository orderRepository,
            OrderOutboxWriter orderOutboxWriter,
            Clock clock,
            OrderMetrics orderMetrics) {
        this.orderRepository =
                Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.orderOutboxWriter =
                Objects.requireNonNull(orderOutboxWriter, "orderOutboxWriter must not be null");
        this.clock =
                Objects.requireNonNull(clock, "clock must not be null");
        this.orderMetrics = orderMetrics;
    }

    @Transactional
    public Order create(CreateOrderCommand command) {
        if (orderMetrics == null) {
            return createOrder(command);
        }

        return orderMetrics.recordOrderCreation(
                () -> createOrder(command));
    }

    private Order createOrder(CreateOrderCommand command) {
        Instant now = Instant.now(clock);

        Order order = Order.place(
                UUID.randomUUID(),
                command.customerId(),
                command.lines().stream()
                        .map(line -> new OrderLine(
                                line.sku(),
                                line.quantity(),
                                line.unitPrice()))
                        .toList(),
                now);

        Order persistedOrder = orderRepository.save(order);

        orderOutboxWriter.save(
                OrderCreatedEvent.from(persistedOrder));

        return persistedOrder;
    }
}
