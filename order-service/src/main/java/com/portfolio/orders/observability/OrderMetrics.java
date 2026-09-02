package com.portfolio.orders.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter orderCreationFailures;
    private final Timer orderCreationDuration;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.ordersCreated = Counter.builder("orders.created.total")
                .description("Total number of successfully created orders")
                .register(meterRegistry);

        this.orderCreationFailures = Counter.builder("orders.creation.failures.total")
                .description("Total number of failed order creation attempts")
                .register(meterRegistry);

        this.orderCreationDuration = Timer.builder("orders.creation.duration")
                .description("Time spent creating orders")
                .register(meterRegistry);
    }

    public <T> T recordOrderCreation(Supplier<T> operation) {
        return orderCreationDuration.record(() -> {
            try {
                T result = operation.get();
                ordersCreated.increment();
                return result;
            } catch (RuntimeException ex) {
                orderCreationFailures.increment();
                throw ex;
            }
        });
    }
}
