package com.portfolio.orders.application;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public CreateOrderService(OrderRepository orderRepository) {
        this(orderRepository, Clock.systemUTC());
    }

    CreateOrderService(OrderRepository orderRepository, Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public Order create(CreateOrderCommand command) {
        Instant now = Instant.now(clock);
        Order order = Order.place(
                UUID.randomUUID(),
                command.customerId(),
                command.lines().stream()
                        .map(line -> new OrderLine(line.sku(), line.quantity(), line.unitPrice()))
                        .toList(),
                now);

        return orderRepository.save(order);
    }
}
