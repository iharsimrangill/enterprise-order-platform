package com.portfolio.orders.cache;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class CachingOrderRepository implements OrderRepository {

    private final OrderRepository delegate;
    private final RedisOrderCache cache;

    public CachingOrderRepository(
            com.portfolio.orders.persistence.OrderPersistenceAdapter delegate,
            RedisOrderCache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Order save(Order order) {
        Order saved = delegate.save(order);
        cache.put(saved);
        return saved;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return cache.get(id)
                .or(() -> {
                    Optional<Order> order = delegate.findById(id);
                    order.ifPresent(cache::put);
                    return order;
                });
    }

    @Override
    public List<Order> findRecentByCustomer(UUID customerId, int page, int size) {
        return delegate.findRecentByCustomer(customerId, page, size);
    }

    @Override
    public List<Order> findRecentByStatus(OrderStatus status, int page, int size) {
        return delegate.findRecentByStatus(status, page, size);
    }
}
