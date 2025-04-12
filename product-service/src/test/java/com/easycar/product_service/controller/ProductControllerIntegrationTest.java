package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.entity.Product;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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

    private final long nonexistentId = 999999L;

    @AfterEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductTests {
        private Product product;

        @BeforeEach
        void setupDbTable() {
            product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());
        }

        @Test
        public void shouldReturnProductById() throws Exception {
            mockMvc.perform(get("/api/products/" + product.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(("Camry")));
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            mockMvc.perform(get("/api/products/" + nonexistentId)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetProductsTests {
        private List<Product> products;
        private final int numberOfProducts = 200;
        private final BigDecimal defaultPrice = BigDecimal.valueOf(100000);

        @BeforeEach
        void setupDbTable() {
            products = IntStream.rangeClosed(1, numberOfProducts)
                    .mapToObj(i -> Product.builder()
                            .name("Product " + i)
                            .description("Description " + i)
                            .price(defaultPrice)
                            .build())
                    .toList();
            productRepository.saveAll(products);
        }

        @Test
        public void shouldReturnProducts() throws Exception {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(100)) // Default page size
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts))
                    .andExpect(jsonPath("$.content[0].id")
                            .value(products.getFirst().getId()))
                    .andExpect(
                            jsonPath("$.content[99].id").value(products.get(99).getId()));
        }

        @Test
        public void shouldReturnProducts_withQueryParams() throws Exception {
            mockMvc.perform(get("/api/products?size=5&page=1&sort=id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts))
                    .andExpect(
                            jsonPath("$.content[0].id").value(products.get(194).getId()))
                    .andExpect(
                            jsonPath("$.content[4].id").value(products.get(190).getId()));
        }

        @Test
        public void shouldReturnProducts_byPrice() throws Exception {
            List<BigDecimal> prices =
                    Arrays.asList(BigDecimal.valueOf(4999), BigDecimal.valueOf(7000), BigDecimal.valueOf(15001));
            prices.forEach((price) -> {
                Product product = Product.builder()
                        .name("Product ")
                        .description("Description ")
                        .price(price)
                        .build();
                productRepository.save(product);
            });

            mockMvc.perform(get("/api/products?minPrice=5000&maxPrice=15000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].price").value("7000.0"));
        }
    }

    @Nested
    @DisplayName("POST /api/products")
    class CreateProductTests {
        @Test
        public void shouldPersistProduct() throws Exception {
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
        public void shouldReturnBadRequest_withEmptyName() throws Exception {
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
        public void shouldReturnBadRequest_withNegativePrice() throws Exception {
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
        private Product product;
        private ProductDto payload;

        @BeforeEach
        void populateDbTable() {
            product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());

            payload = new ProductDto();
            payload.setName("CR-V");
            payload.setDescription("Cool SUV");
            payload.setPrice(BigDecimal.valueOf(65000));
        }

        @Test
        public void shouldUpdateProduct() throws Exception {
            mockMvc.perform(patch("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk());

            Optional<Product> updated = productRepository.findById(product.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getName()).isEqualTo("CR-V");
            assertThat(updated.get().getDescription()).isEqualTo("Cool SUV");
            assertThat(updated.get().getPrice().compareTo(BigDecimal.valueOf(65000)))
                    .isZero();
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            mockMvc.perform(patch("/api/products/" + nonexistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnBadRequest_withEmptyName() throws Exception {
            payload.setName("");

            mockMvc.perform(patch("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldReturnBadRequest_withEmptyDescription() throws Exception {
            payload.setDescription("");

            mockMvc.perform(patch("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldReturnBadRequest_withNegativePrice() throws Exception {
            payload.setPrice(BigDecimal.valueOf(-54300));

            mockMvc.perform(patch("/api/products/" + product.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProductTests {
        private Product product;

        @BeforeEach
        void setupProductDb() {
            product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .build());
        }

        @Test
        public void shouldDeleteProduct() throws Exception {
            mockMvc.perform(delete("/api/products/" + product.getId()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            Optional<Product> updated = productRepository.findById(product.getId());
            assertThat(updated).isNotPresent();
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            mockMvc.perform(delete("/api/products/" + nonexistentId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
