package com.easycar.product_service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.config.TestRedisConfiguration;
import com.easycar.product_service.domain.Category;
import com.easycar.product_service.domain.Make;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.helper.EntityBuilder;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestRedisConfiguration.class)
@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableCaching
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

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void cleanDb() {
        productRepository.deleteAll();
        dealerRepository.deleteAll();
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
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

    @Nested
    @DisplayName("GET /api/products")
    class GetProductsTests {
        private List<Product> products;
        private Dealer dealer;
        private final int numberOfProducts = 50;
        private final BigDecimal defaultPrice = BigDecimal.valueOf(100000);

        @BeforeEach
        void setup() {
            products = new ArrayList<>();
            dealer = dealerRepository.save(Dealer.builder()
                    .name("Sample Dealer")
                    .address("123 Main Street, Springfield")
                    .build());
        }

        @Test
        public void shouldReturnLatest10AvailableProducts_whenNoParameterIsUsed() throws Exception {
            for (int i = 0; i < numberOfProducts; i++) {
                var product = EntityBuilder.buildDefaultProductBuilder(dealer)
                        .available(i != 41)
                        .build();
                productRepository.save(product);
                product.setCreatedAt(LocalDateTime.now().minusDays(numberOfProducts - i));
                products.add(product);
                productRepository.save(product);
            }

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts - 1))
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
            for (int i = 0; i < numberOfProducts; i++) {
                var product = EntityBuilder.buildDefaultProductBuilder(dealer)
                        .available(i != 11)
                        .build();
                productRepository.save(product);
                product.setCreatedAt(LocalDateTime.now().minusDays(numberOfProducts - i));
                products.add(product);
                productRepository.save(product);
            }

            mockMvc.perform(get("/api/products?page=1&sort=createdAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements").value(numberOfProducts - 1))
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

        @Test
        public void shouldReturnAvailableProductsByPrice() throws Exception {
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> EntityBuilder.buildDefaultProductBuilder(dealer)
                            .price(BigDecimal.valueOf(i))
                            .build())
                    .toList();
            productRepository.saveAll(products);

            var minPrice = BigDecimal.valueOf(10);
            var maxPrice = BigDecimal.valueOf(11);
            mockMvc.perform(get("/api/products?sort=price,asc&minPrice=" + minPrice + "&maxPrice=" + maxPrice))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].price").value("10.0"))
                    .andExpect(jsonPath("$.content[1].price").value("11.0"));
        }

        @Test
        public void shouldReturnAvailableProductsByMileage() throws Exception {
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> EntityBuilder.buildDefaultProductBuilder(dealer)
                            .mileage(i)
                            .build())
                    .toList();
            productRepository.saveAll(products);

            var minMileage = 10;
            var maxMileage = 12;
            mockMvc.perform(get("/api/products?sort=price,asc&minMileage=" + minMileage + "&maxMileage=" + maxMileage))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.content[0].mileage").value("10"))
                    .andExpect(jsonPath("$.content[1].mileage").value("11"))
                    .andExpect(jsonPath("$.content[2].mileage").value("12"));
        }

        @Test
        public void shouldReturnAvailableProductsByMake() throws Exception {
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> {
                        Make make;
                        if (i < 2) {
                            make = Make.BMW;
                        } else if (i < 4) {
                            make = Make.VOLKSWAGEN;
                        } else {
                            make = Make.FORD;
                        }

                        return EntityBuilder.buildDefaultProductBuilder(dealer)
                                .make(make)
                                .build();
                    })
                    .toList();
            productRepository.saveAll(products);

            Make[] makes = {Make.BMW, Make.HONDA, Make.VOLKSWAGEN};
            String makesString = Arrays.stream(makes).map(Make::name).collect(Collectors.joining(","));
            mockMvc.perform(get("/api/products?makes=" + makesString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4)))
                    .andExpect(jsonPath("$.totalElements").value(4))
                    .andExpect(jsonPath("$.content[*].make")
                            .value(Matchers.hasItems(Make.BMW.toString(), Make.VOLKSWAGEN.toString())));
        }

        @Test
        public void shouldReturnAvailableProductsByNames() throws Exception {
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> {
                        String name;
                        if (i == 0 || i == numberOfProducts - 1) {
                            name = "BMW 1 Series";
                        } else {
                            name = "BMW 2 Series";
                        }

                        return EntityBuilder.buildDefaultProductBuilder(dealer)
                                .name(name)
                                .build();
                    })
                    .toList();
            productRepository.saveAll(products);

            mockMvc.perform(get("/api/products?name=1 s"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("BMW 1 Series"))
                    .andExpect(jsonPath("$.content[1].name").value("BMW 1 Series"));
        }

        @Test
        public void shouldReturnAvailableProductsByDealerIds() throws Exception {
            Dealer dealerA = dealerRepository.save(
                    Dealer.builder().name("Dealer A").address("Sample street 1").build());
            Dealer dealerB = dealerRepository.save(
                    Dealer.builder().name("Dealer B").address("Sample street 2").build());
            Dealer dealerC = dealerRepository.save(
                    Dealer.builder().name("Dealer C").address("Sample street 3").build());
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> {
                        Dealer dealer;
                        if (i == 5) {
                            dealer = dealerA;
                        } else if (i == 10) {
                            dealer = dealerB;
                        } else {
                            dealer = dealerC;
                        }

                        return EntityBuilder.buildDefaultProductBuilder(dealer)
                                .dealer(dealer)
                                .build();
                    })
                    .toList();
            productRepository.saveAll(products);

            String dealerIds = Stream.of(dealerA, dealerB)
                    .map(dealer -> dealer.getId().toString())
                    .collect(Collectors.joining(","));

            mockMvc.perform(get("/api/products?dealerIds=" + dealerIds))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[*].dealerId")
                            .value(Matchers.containsInAnyOrder(
                                    dealerA.getId().intValue(), dealerB.getId().intValue())));
        }

        @Test
        public void shouldReturnBadRequest_whenIdIsUsedForSorting() throws Exception {
            products = IntStream.range(0, numberOfProducts)
                    .mapToObj(i -> EntityBuilder.buildDefaultProductBuilder(dealer)
                            .dealer(dealer)
                            .build())
                    .toList();
            productRepository.saveAll(products);

            mockMvc.perform(get("/api/products?sort=id")).andExpect(status().isBadRequest());
        }
    }
}
