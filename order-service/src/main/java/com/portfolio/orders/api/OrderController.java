package com.portfolio.orders.api;

import com.portfolio.orders.application.CreateOrderCommand;
import com.portfolio.orders.application.CreateOrderService;
import com.portfolio.orders.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderService createOrderService;

    public OrderController(CreateOrderService createOrderService) {
        this.createOrderService = createOrderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.lines().stream()
                        .map(line -> new CreateOrderCommand.Line(
                                line.sku(),
                                line.quantity(),
                                line.unitPrice()))
                        .toList());

        Order order = createOrderService.create(command);
        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + order.id()))
                .body(OrderResponse.from(order));
    }
}
