package com.portfolio.orders.persistence;

import com.portfolio.orders.domain.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrderRepository
        extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = "lines")
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(
            UUID customerId,
            Pageable pageable);

    @EntityGraph(attributePaths = "lines")
    List<OrderEntity> findByStatusOrderByCreatedAtDesc(
            OrderStatus status,
            Pageable pageable);
}
