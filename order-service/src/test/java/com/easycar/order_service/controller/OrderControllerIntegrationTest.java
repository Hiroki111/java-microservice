package com.easycar.order_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    @DisplayName("POST /api/orders")
    class CreateOrderTests {
        long productId = 1;
        String customerName = "John Smith";

        @Test
        public void shouldPersistOrder() throws Exception {
            ProductDto mockedProduct =
                    ProductDto.builder().id(productId).available(true).build();
            ResponseEntity<ProductDto> response =
                    new ResponseEntity<ProductDto>(mockedProduct, HttpStatusCode.valueOf(201));
            when(productServiceFeignClient.fetchProduct(productId)).thenReturn(response);

            OrderCreateDto payload = OrderCreateDto.builder()
                    .productId(productId)
                    .customerName(customerName)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
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
            when(productServiceFeignClient.fetchProduct(productId)).thenThrow(notFound);

            OrderCreateDto payload = OrderCreateDto.builder()
                    .productId(productId)
                    .customerName(customerName)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());

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
            ResponseEntity<ProductDto> response =
                    new ResponseEntity<ProductDto>(mockedProduct, HttpStatusCode.valueOf(201));
            when(productServiceFeignClient.fetchProduct(productId)).thenReturn(response);

            OrderCreateDto payload = OrderCreateDto.builder()
                    .productId(productId)
                    .customerName(customerName)
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
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
