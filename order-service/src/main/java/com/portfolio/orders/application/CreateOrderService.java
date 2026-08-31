package com.portfolio.orders.application;

import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderOutboxWriter;
import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
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

    @Autowired
    public CreateOrderService(
            OrderRepository orderRepository,
            OrderOutboxWriter orderOutboxWriter) {
        this(orderRepository, orderOutboxWriter, Clock.systemUTC());
    }

    CreateOrderService(
            OrderRepository orderRepository,
            OrderOutboxWriter orderOutboxWriter,
            Clock clock) {
        this.orderRepository =
                Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.orderOutboxWriter =
                Objects.requireNonNull(orderOutboxWriter, "orderOutboxWriter must not be null");
        this.clock =
                Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public Order create(CreateOrderCommand command) {
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
