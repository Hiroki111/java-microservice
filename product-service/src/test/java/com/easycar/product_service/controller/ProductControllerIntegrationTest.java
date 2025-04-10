package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.entity.Product;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
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

    private record ProductTestDto(String name, String description, BigDecimal price, Boolean available) {}

    @AfterEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductTests {
        @Test
        public void testGetProduct_shouldReturnProductById() throws Exception {
            var saved = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());

            mockMvc.perform(get("/api/products/" + saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(("Camry")));
        }

        @Test
        public void testGetProduct_withEmptyDb_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/products/1")).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetProductsTests {

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

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProductTests {
        @Test
        public void testUpdateProduct_shouldUpdateProduct() throws Exception {
            Product product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());
            ProductDto payload = new ProductDto();
            payload.setName("CR-V");
            payload.setDescription("Cool SUV");
            payload.setPrice(BigDecimal.valueOf(65000));

            mockMvc.perform(patch("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk());

            Optional<Product> updated = productRepository.findById(product.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getName()).isEqualTo("CR-V");
            assertThat(updated.get().getDescription()).isEqualTo("Cool SUV");
            assertThat(updated.get().getPrice().compareTo(BigDecimal.valueOf(65000))).isZero();
        }

        @Test
        public void testUpdateProduct_withEmptyDb_shouldReturnNotFound() throws Exception {
            ProductDto payload = new ProductDto();
            payload.setName("CR-V");

            mockMvc.perform(patch("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void testUpdateProduct_withEmptyName_shouldReturnBadRequest() throws Exception {
            ProductDto payload = new ProductDto();
            payload.setName("");

            mockMvc.perform(patch("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProductTests {
        @Test
        public void testDeleteProduct_shouldDeleteProduct() throws Exception {
            Product product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());

            mockMvc.perform(delete("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            Optional<Product> updated = productRepository.findById(product.getId());
            assertThat(updated).isNotPresent();
        }

        @Test
        public void testDeleteProduct_withEmptyDb_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
