package com.portfolio.orders.application.port;

import com.portfolio.orders.application.event.OrderCreatedEvent;

public interface OrderEventPublisher {
    void publish(OrderCreatedEvent event);
}
