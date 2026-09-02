package com.portfolio.orders.api;

import com.portfolio.orders.application.CreateOrderService;
import com.portfolio.orders.application.OrderQueryService;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderSecurityTest {

    private static final UUID ORDER_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID CUSTOMER_ID =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateOrderService createOrderService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    void rejectsUnauthenticatedOrderRead() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("customerId", CUSTOMER_ID.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
        void allowsAuthenticatedReaderToQueryOrders() throws Exception {
        when(orderQueryService.findByCustomer(
                any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders").with(httpBasic("order-reader", "reader-change-me"))
                        .param("customerId", CUSTOMER_ID.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
        void rejectsReaderCreatingOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders").with(httpBasic("order-reader", "reader-change-me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsWriterCreatingOrder() throws Exception {
        Order order = Order.place(
                ORDER_ID,
                CUSTOMER_ID,
                List.of(new OrderLine(
                        "SKU-100",
                        2,
                        new BigDecimal("12.50"))),
                Instant.parse("2026-09-02T12:00:00Z"));

        when(createOrderService.create(any())).thenReturn(order);

        mockMvc.perform(post("/api/v1/orders").with(httpBasic("order-writer", "writer-change-me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderRequest()))
                .andExpect(status().isCreated());
    }

    private String validOrderRequest() {
        return """
                {
                  "customerId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                  "lines": [
                    {
                      "sku": "SKU-100",
                      "quantity": 2,
                      "unitPrice": 12.50
                    }
                  ]
                }
                """;
    }
}
