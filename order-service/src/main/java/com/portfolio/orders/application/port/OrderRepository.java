package com.portfolio.orders.application.port;

import com.portfolio.orders.domain.Order;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for the order aggregate.
 *
 * <p>The application layer depends on this abstraction rather than Spring Data,
 * keeping the domain and use cases independent of persistence technology.</p>
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);
}
