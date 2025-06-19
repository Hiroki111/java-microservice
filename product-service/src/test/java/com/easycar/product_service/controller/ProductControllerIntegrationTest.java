package com.easycar.product_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.Category;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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

    private record ProductTestDto(
            String name, String description, BigDecimal price, Boolean available, Category category, Long dealerId) {}

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
}
