package com.portfolio.orders.api;

import com.portfolio.orders.application.CreateOrderCommand;
import com.portfolio.orders.application.CreateOrderService;
import com.portfolio.orders.application.OrderQueryService;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final OrderQueryService orderQueryService;

    public OrderController(
            CreateOrderService createOrderService,
            OrderQueryService orderQueryService) {
        this.createOrderService = createOrderService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request) {

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

    @GetMapping(params = "customerId")
    public ResponseEntity<List<OrderResponse>> findByCustomer(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<OrderResponse> response =
                orderQueryService
                        .findByCustomer(customerId, page, size)
                        .stream()
                        .map(OrderResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "status")
    public ResponseEntity<List<OrderResponse>> findByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<OrderResponse> response =
                orderQueryService
                        .findByStatus(status, page, size)
                        .stream()
                        .map(OrderResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }
}
