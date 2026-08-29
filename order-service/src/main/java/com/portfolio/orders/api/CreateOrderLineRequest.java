package com.portfolio.orders.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderLineRequest(
        @NotBlank(message = "sku is required") String sku,
        @Positive(message = "quantity must be greater than zero") int quantity,
        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "unitPrice must not be negative") BigDecimal unitPrice) {
}
