package com.portfolio.orders.persistence;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class OrderPersistenceAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public OrderPersistenceAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity saved = repository.save(OrderPersistenceMapper.toEntity(order));
        return OrderPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(OrderPersistenceMapper::toDomain);
    }
}
