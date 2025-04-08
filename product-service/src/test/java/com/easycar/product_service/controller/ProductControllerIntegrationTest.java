package com.easycar.product_service.controller;

import com.easycar.product_service.entity.Product;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateProduct_shouldPersistProduct() throws Exception {
        String productName = "Toyota Corolla";
        var productTestDto = new ProductTestDto(
            productName,
            "A popular sedan",
            BigDecimal.valueOf(20000),
            true
        );

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productTestDto)))
                .andExpect(status().isCreated());

        Optional<Product> saved = productRepository.findAll()
                .stream()
                .filter(product -> product.getName().equals(productName))
                .findFirst();

        assertThat(saved).isPresent();
        assertThat(saved.get().getDescription()).isEqualTo(productTestDto.description);
    }

    private record ProductTestDto(
        String name,
        String description,
        BigDecimal price,
        boolean available
    ) {}
}
