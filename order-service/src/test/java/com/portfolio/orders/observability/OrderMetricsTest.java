package com.portfolio.orders.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderMetricsTest {

    @Test
    void recordsSuccessfulOrderCreation() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrderMetrics metrics = new OrderMetrics(registry);

        String result = metrics.recordOrderCreation(() -> "created");

        assertEquals("created", result);
        assertEquals(1.0,
                registry.get("orders.created.total").counter().count());
        assertEquals(0.0,
                registry.get("orders.creation.failures.total").counter().count());
        assertEquals(1,
                registry.get("orders.creation.duration").timer().count());
    }

    @Test
    void recordsFailedOrderCreationAndRethrowsException() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrderMetrics metrics = new OrderMetrics(registry);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> metrics.recordOrderCreation(() -> {
                    throw new IllegalStateException("creation failed");
                }));

        assertEquals("creation failed", exception.getMessage());
        assertEquals(0.0,
                registry.get("orders.created.total").counter().count());
        assertEquals(1.0,
                registry.get("orders.creation.failures.total").counter().count());
        assertEquals(1,
                registry.get("orders.creation.duration").timer().count());
    }
}
