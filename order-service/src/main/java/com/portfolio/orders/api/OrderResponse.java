package com.portfolio.orders.api;

import com.portfolio.orders.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderLineResponse> lines,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.status().name(),
                order.totalAmount(),
                order.lines().stream()
                        .map(line -> new OrderLineResponse(
                                line.sku(),
                                line.quantity(),
                                line.unitPrice(),
                                line.subtotal()))
                        .toList(),
                order.createdAt(),
                order.updatedAt());
    }
}
