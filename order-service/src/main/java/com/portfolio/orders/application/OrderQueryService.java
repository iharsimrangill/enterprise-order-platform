package com.portfolio.orders.application;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository =
                Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(UUID customerId, int page, int size) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        validatePage(page, size);

        return orderRepository.findRecentByCustomer(customerId, page, size);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status, int page, int size) {
        Objects.requireNonNull(status, "status must not be null");
        validatePage(page, size);

        return orderRepository.findRecentByStatus(status, page, size);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
