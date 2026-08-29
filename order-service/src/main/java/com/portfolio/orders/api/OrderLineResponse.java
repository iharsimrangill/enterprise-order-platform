package com.portfolio.orders.api;

import java.math.BigDecimal;

public record OrderLineResponse(
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
