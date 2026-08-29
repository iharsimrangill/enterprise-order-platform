package com.portfolio.orders.application;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateOrderService {

    private final Clock clock;

    public CreateOrderService() {
        this(Clock.systemUTC());
    }

    CreateOrderService(Clock clock) {
        this.clock = clock;
    }

    public Order create(CreateOrderCommand command) {
        Instant now = Instant.now(clock);
        return Order.place(
                UUID.randomUUID(),
                command.customerId(),
                command.lines().stream()
                        .map(line -> new OrderLine(line.sku(), line.quantity(), line.unitPrice()))
                        .toList(),
                now);
    }
}
