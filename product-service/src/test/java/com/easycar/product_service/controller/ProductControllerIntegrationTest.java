package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.entity.Product;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String productName;

    private record ProductTestDto(String name, String description, BigDecimal price, Boolean available) {}

    @BeforeEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/products")
    class CreateProductTests {
        @Test
        public void testCreateProduct_shouldPersistProduct() throws Exception {
            String productName = "Toyota Corolla";
            var productTestDto = new ProductTestDto(productName, "A popular sedan", BigDecimal.valueOf(20000), true);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productTestDto)))
                    .andExpect(status().isCreated());

            Optional<Product> saved = productRepository.findAll().stream()
                    .filter(product -> product.getName().equals(productName))
                    .findFirst();

            assertThat(saved).isPresent();
            assertThat(saved.get().getDescription()).isEqualTo(productTestDto.description);
        }

        @Test
        public void testCreateProduct_withEmptyName_shouldReturnBadRequest() throws Exception {
            String productName = "";
            var productTestDto = new ProductTestDto(productName, "A popular sedan", BigDecimal.valueOf(20000), true);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productTestDto)))
                    .andExpect(status().isBadRequest());

            Optional<Product> saved = productRepository.findAll().stream()
                    .filter(product -> product.getName().equals(productName))
                    .findFirst();

            assertThat(saved).isNotPresent();
        }

        @Test
        public void testCreateProduct_withNegativePrice_shouldReturnBadRequest() throws Exception {
            String productName = "Toyota Corolla";
            var productTestDto = new ProductTestDto(productName, "Desc", BigDecimal.valueOf(-20000), true);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productTestDto)))
                    .andExpect(status().isBadRequest());

            Optional<Product> saved = productRepository.findAll().stream()
                    .filter(product -> product.getName().equals(productName))
                    .findFirst();

            assertThat(saved).isNotPresent();
        }
    }
}
