package com.portfolio.orders.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(UUID customerId, List<Line> lines) {

    public CreateOrderCommand {
        lines = List.copyOf(lines);
    }

    public record Line(String sku, int quantity, BigDecimal unitPrice) {
    }
}
