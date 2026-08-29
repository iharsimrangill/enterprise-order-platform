package com.portfolio.orders.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "customerId is required") UUID customerId,
        @NotEmpty(message = "at least one order line is required")
        List<@Valid CreateOrderLineRequest> lines) {
}
