package com.easycar.order_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.ProductDto;
import com.easycar.order_service.repository.OrderRepository;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductServiceFeignClient productServiceFeignClient;

    @AfterEach
    void cleanDb() {
        orderRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetOrdersTests {
        private List<Order> orders;
        private final int numberOfOrders = 200;

        @BeforeEach
        void setup() {
            orders = IntStream.rangeClosed(1, numberOfOrders)
                    .mapToObj(i -> Order.builder()
                            .customerName("Customer " + i)
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
                    .andExpect(jsonPath("$.content[0].id")
                            .value(orders.getFirst().getId()))
                    .andExpect(
                            jsonPath("$.content[99].id").value(orders.get(99).getId()));
        }

        @Test
        public void shouldReturnOrders_withQueryParams() throws Exception {
            mockMvc.perform(get("/api/orders?size=5&page=1&sort=id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.totalElements").value(numberOfOrders))
                    .andExpect(
                            jsonPath("$.content[0].id").value(orders.get(194).getId()))
                    .andExpect(
                            jsonPath("$.content[4].id").value(orders.get(190).getId()));
        }
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {
        long productId = 1;
        String customerName = "John Smith";
        OrderCreateDto payload;
        String correlationId = "1";

        @BeforeEach
        void setup() {
            payload = OrderCreateDto.builder()
                    .productId(productId)
                    .customerName(customerName)
                    .build();
        }

        @Test
        public void shouldPersistOrder() throws Exception {
            ProductDto mockedProduct =
                    ProductDto.builder().id(productId).available(true).build();
            ResponseEntity<ProductDto> response = new ResponseEntity<>(mockedProduct, HttpStatusCode.valueOf(201));
            when(productServiceFeignClient.fetchProduct(correlationId, productId))
                    .thenReturn(response);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("easycar-correlation-id", correlationId)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            Optional<Order> saved = orderRepository.findAll().stream()
                    .filter(order -> order.getProductId().equals(productId)
                            && order.getCustomerName().equals(customerName))
                    .findFirst();

            assertThat(saved).isPresent();
        }

        @Test
        public void shouldReturnNotFound_withProductNotFound() throws Exception {
            Request request = Request.create(
                    Request.HttpMethod.GET,
                    "/api/products/" + productId,
                    Map.of(),
                    null,
                    Charset.defaultCharset(),
                    null);
            FeignException notFound = new FeignException.NotFound("Product not found", request, null, null);
            NoFallbackAvailableException exception = new NoFallbackAvailableException("fetchProduct failed", notFound);
            when(productServiceFeignClient.fetchProduct(correlationId, productId))
                    .thenThrow(exception);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("easycar-correlation-id", correlationId)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

            Optional<Order> saved = orderRepository.findAll().stream()
                    .filter(order -> order.getProductId().equals(productId)
                            && order.getCustomerName().equals(customerName))
                    .findFirst();

            assertThat(saved).isNotPresent();
        }

        @Test
        public void shouldReturnServiceUnavailable_withGenericException() throws Exception {
            Request request = Request.create(
                    Request.HttpMethod.GET,
                    "/api/products/" + productId,
                    Map.of(),
                    null,
                    Charset.defaultCharset(),
                    null);
            Exception cause = new Exception("Internal error");
            NoFallbackAvailableException exception =
                    new NoFallbackAvailableException("Downstream service unavailable", cause);
            when(productServiceFeignClient.fetchProduct(correlationId, productId))
                    .thenThrow(exception);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("easycar-correlation-id", correlationId)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isServiceUnavailable());

            Optional<Order> saved = orderRepository.findAll().stream()
                    .filter(order -> order.getProductId().equals(productId)
                            && order.getCustomerName().equals(customerName))
                    .findFirst();

            assertThat(saved).isNotPresent();
        }

        @Test
        public void shouldReturnConflict_withProductNotAvailable() throws Exception {
            ProductDto mockedProduct =
                    ProductDto.builder().id(productId).available(false).build();
            ResponseEntity<ProductDto> response = new ResponseEntity<>(mockedProduct, HttpStatusCode.valueOf(201));
            when(productServiceFeignClient.fetchProduct(correlationId, productId))
                    .thenReturn(response);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("easycar-correlation-id", correlationId)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isConflict());

            Optional<Order> saved = orderRepository.findAll().stream()
                    .filter(order -> order.getProductId().equals(productId)
                            && order.getCustomerName().equals(customerName))
                    .findFirst();

            assertThat(saved).isNotPresent();
        }
    }
}
