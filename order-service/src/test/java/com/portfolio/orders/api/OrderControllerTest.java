package com.portfolio.orders.api;

import com.portfolio.orders.application.CreateOrderService;
import com.portfolio.orders.application.OrderQueryService;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final UUID ORDER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CUSTOMER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final Instant NOW = Instant.parse("2026-08-29T15:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateOrderService createOrderService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    void returns201ForValidOrder() throws Exception {
        Order order = Order.place(
                ORDER_ID,
                CUSTOMER_ID,
                List.of(new OrderLine("SKU-100", 2, new BigDecimal("12.50"))),
                NOW);
        when(createOrderService.create(any())).thenReturn(order);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                  "lines": [
                                    {"sku": "SKU-100", "quantity": 2, "unitPrice": 12.50}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/" + ORDER_ID))
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(25.00))
                .andExpect(jsonPath("$.lines[0].sku").value("SKU-100"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));
    }

    @Test
    void returns400WithFieldErrorsForInvalidOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": null,
                                  "lines": [
                                    {"sku": "", "quantity": 0, "unitPrice": -1}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("customerId")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("lines[0].sku")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("lines[0].quantity")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("lines[0].unitPrice")));
    }

    @Test
    void returns400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void returnsRecentOrdersForCustomer() throws Exception {
        when(orderQueryService.findByCustomer(any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders")
                        .param("customerId", CUSTOMER_ID.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsRecentOrdersForStatus() throws Exception {
        when(orderQueryService.findByStatus(any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

}
