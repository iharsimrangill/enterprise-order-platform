package com.portfolio.orders.application.port;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    default List<Order> findRecentByCustomer(UUID customerId, int page, int size) {
        throw new UnsupportedOperationException("Customer order queries are not supported");
    }

    default List<Order> findRecentByStatus(OrderStatus status, int page, int size) {
        throw new UnsupportedOperationException("Status order queries are not supported");
    }
}
