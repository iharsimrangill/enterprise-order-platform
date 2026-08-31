package com.portfolio.orders.persistence;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public List<Order> findRecentByCustomer(UUID customerId, int page, int size) {
        return repository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId,
                        PageRequest.of(page, size))
                .stream()
                .map(OrderPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findRecentByStatus(OrderStatus status, int page, int size) {
        return repository
                .findByStatusOrderByCreatedAtDesc(
                        status,
                        PageRequest.of(page, size))
                .stream()
                .map(OrderPersistenceMapper::toDomain)
                .toList();
    }
}
