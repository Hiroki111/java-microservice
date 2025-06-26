package com.easycar.product_service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.Category;
import com.easycar.product_service.domain.Make;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanDb() {
        productRepository.deleteAll();
        dealerRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductTests {
        private Product product;
        String correlationId = "1";

        @BeforeEach
        void setupDbTable() {
            Dealer dealer = dealerRepository.save(Dealer.builder()
                    .name("Sunshine Auto")
                    .address("123 Main Street, Springfield")
                    .build());

            product = productRepository.save(Product.builder()
                    .name("Camry")
                    .description("Reliable car")
                    .price(BigDecimal.valueOf(55000))
                    .category(Category.SEDAN)
                    .make(Make.TOYOTA)
                    .mileage(1000)
                    .dealer(dealer)
                    .build());
        }

        @Test
        public void shouldReturnProductById() throws Exception {
            mockMvc.perform(get("/api/products/" + product.getId()).header("easycar-correlation-id", correlationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(("Camry")));
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            long nonexistentId = 999999L;
            mockMvc.perform(get("/api/products/" + nonexistentId).header("easycar-correlation-id", correlationId))
                    .andExpect(status().isNotFound());
        }
    }

    // 1) The endpoint returns the latest 10 available products
    // 2) The endpoint filters the output by price range, mileage range, makes, car names, dealer IDs
    @Nested
    @DisplayName("GET /api/products")
    class GetProductsTests {
        private List<Product> products;
        private Dealer dealer;
        private final int numberOfProducts = 50;
        private final BigDecimal defaultPrice = BigDecimal.valueOf(100000);

        @BeforeEach
        void setupDbTables() {
            products = new ArrayList<>();
            dealer = dealerRepository.save(Dealer.builder()
                    .name("Dealer A")
                    .address("123 Main Street, Springfield")
                    .build());

            for (int i = 0; i < numberOfProducts; i++) {
                var product = Product.builder()
                        .name("Product " + i)
                        .description("Description " + i)
                        .price(defaultPrice)
                        .category(Category.SEDAN)
                        .make(Make.BMW)
                        .mileage(1000)
                        .dealer(dealer)
                        // ID 11 and 41 are unavailable
                        .available(i != 11 && i != 41)
                        .build();
                product.setCreatedAt(LocalDateTime.now().minusDays(numberOfProducts - i));
                products.add(product);
                productRepository.save(product);
            }
        }

        @Test
        public void shouldReturnLatest10AvailableProducts() throws Exception {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts - 2))
                    .andExpect(
                            jsonPath("$.content[0].id").value(products.get(49).getId()))
                    .andExpect(
                            jsonPath("$.content[1].id").value(products.get(48).getId()))
                    .andExpect(
                            jsonPath("$.content[2].id").value(products.get(47).getId()))
                    .andExpect(
                            jsonPath("$.content[3].id").value(products.get(46).getId()))
                    .andExpect(
                            jsonPath("$.content[4].id").value(products.get(45).getId()))
                    .andExpect(
                            jsonPath("$.content[5].id").value(products.get(44).getId()))
                    .andExpect(
                            jsonPath("$.content[6].id").value(products.get(43).getId()))
                    .andExpect(
                            jsonPath("$.content[7].id").value(products.get(42).getId()))
                    // ID 41 is unavailable
                    .andExpect(
                            jsonPath("$.content[8].id").value(products.get(40).getId()))
                    .andExpect(
                            jsonPath("$.content[9].id").value(products.get(39).getId()));
        }

        @Test
        public void shouldReturnSecondPageOf10AvailableProducts_whenSortedByCreatedAtAsc() throws Exception {
            var productsSortedByCreatedAt = products.stream()
                    .sorted(Comparator.comparing(Product::getCreatedAt))
                    .toList();

            mockMvc.perform(get("/api/products?page=1&sort=createdAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts - 2))
                    .andExpect(
                            jsonPath("$.content[0].id").value(products.get(10).getId()))
                    // ID 11 is unavailable
                    .andExpect(
                            jsonPath("$.content[1].id").value(products.get(12).getId()))
                    .andExpect(
                            jsonPath("$.content[2].id").value(products.get(13).getId()))
                    .andExpect(
                            jsonPath("$.content[3].id").value(products.get(14).getId()))
                    .andExpect(
                            jsonPath("$.content[4].id").value(products.get(15).getId()))
                    .andExpect(
                            jsonPath("$.content[5].id").value(products.get(16).getId()))
                    .andExpect(
                            jsonPath("$.content[6].id").value(products.get(17).getId()))
                    .andExpect(
                            jsonPath("$.content[7].id").value(products.get(18).getId()))
                    .andExpect(
                            jsonPath("$.content[8].id").value(products.get(19).getId()))
                    .andExpect(
                            jsonPath("$.content[9].id").value(products.get(20).getId()));
        }
    }
}
