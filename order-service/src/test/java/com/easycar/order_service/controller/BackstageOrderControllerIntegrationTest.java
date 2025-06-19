package com.easycar.order_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.repository.OrderRepository;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class BackstageOrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductServiceFeignClient productServiceFeignClient;

    @MockitoBean
    private StreamBridge streamBridge;

    @AfterEach
    void cleanDb() {
        orderRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetOrdersTests {
        private List<Order> orders;
        private final String customerId = "5c850b3f-8a18-4b2a-b112-f82d8e3e6c6e";
        private final int numberOfOrders = 200;

        @BeforeEach
        void setup() {
            orders = IntStream.rangeClosed(1, numberOfOrders)
                    .mapToObj(i -> Order.builder()
                            .customerName("Customer " + i)
                            .customerId(customerId)
                            .productId((long) i)
                            .build())
                    .toList();
            orderRepository.saveAll(orders);
        }

        @Test
        public void shouldReturnOrders() throws Exception {
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(100)) // Default page size
                    .andExpect(jsonPath("$.totalElements").value(numberOfOrders))
                    .andExpect(
                            jsonPath("$.content[0].id").value(orders.getFirst().getId()))
                    .andExpect(jsonPath("$.content[99].id").value(orders.get(99).getId()));
        }

        @Test
        public void shouldReturnOrders_withQueryParams() throws Exception {
            mockMvc.perform(get("/api/orders?size=5&page=1&sort=id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.totalElements").value(numberOfOrders))
                    .andExpect(jsonPath("$.content[0].id").value(orders.get(194).getId()))
                    .andExpect(jsonPath("$.content[4].id").value(orders.get(190).getId()));
        }
    }
}
