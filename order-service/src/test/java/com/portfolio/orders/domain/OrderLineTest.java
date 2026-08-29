package com.portfolio.orders.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderLineTest {

    @Test
    void calculatesSubtotalUsingMoneyScale() {
        OrderLine line = new OrderLine("SKU-100", 3, new BigDecimal("19.995"));

        assertEquals(new BigDecimal("60.00"), line.subtotal());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderLine("SKU-100", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderLine("SKU-100", 1, new BigDecimal("-0.01")));
    }
}
