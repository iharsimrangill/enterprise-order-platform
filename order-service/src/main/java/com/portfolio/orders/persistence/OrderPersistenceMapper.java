package com.portfolio.orders.persistence;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;

import java.util.Comparator;
import java.util.List;

final class OrderPersistenceMapper {

    private OrderPersistenceMapper() {
    }

    static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(
                order.id(),
                order.customerId(),
                order.status(),
                order.createdAt(),
                order.updatedAt(),
                order.rejectionReason());

        for (int index = 0; index < order.lines().size(); index++) {
            OrderLine line = order.lines().get(index);
            entity.addLine(new OrderLineEntity(
                    new OrderLineId(order.id(), index + 1),
                    line.sku(),
                    line.quantity(),
                    line.unitPrice()));
        }

        return entity;
    }

    static Order toDomain(OrderEntity entity) {
        List<OrderLine> lines = entity.lines().stream()
                .sorted(Comparator.comparingInt(line -> line.id().lineNumber()))
                .map(line -> new OrderLine(line.sku(), line.quantity(), line.unitPrice()))
                .toList();

        return Order.restore(
                entity.id(),
                entity.customerId(),
                lines,
                entity.status(),
                entity.createdAt(),
                entity.updatedAt(),
                entity.rejectionReason());
    }
}
