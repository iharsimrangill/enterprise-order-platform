package com.portfolio.orders.application.port;

import com.portfolio.orders.application.event.OrderCreatedEvent;

public interface OrderOutboxWriter {
    void save(OrderCreatedEvent event);
}
