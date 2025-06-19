package com.easycar.product_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.repository.DealerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.IntStream;
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
public class DealerControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DealerRepository dealerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanDb() {
        dealerRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/dealers/{id}")
    class GetDealerTests {
        private Dealer dealer;

        @BeforeEach
        void setupDbTable() {
            dealer = dealerRepository.save(Dealer.builder()
                    .name("Sunshine Auto")
                    .address("123 Main Street, Springfield")
                    .build());
        }

        @Test
        public void shouldReturnDealerById() throws Exception {
            mockMvc.perform(get("/api/dealers/" + dealer.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value((dealer.getId())));
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            long nonexistentId = 999999L;
            mockMvc.perform(get("/api/dealers/" + nonexistentId)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/dealers")
    class GetDealersTests {
        private List<Dealer> dealers;
        private final int numberOfDealers = 200;

        @BeforeEach
        void setupDbTables() {
            dealers = IntStream.rangeClosed(1, numberOfDealers)
                    .mapToObj(i -> Dealer.builder()
                            .name("Dealer " + i)
                            .address("Address " + i)
                            .build())
                    .toList();
            dealerRepository.saveAll(dealers);
        }

        @Test
        public void shouldReturnDealers() throws Exception {
            mockMvc.perform(get("/api/dealers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(100)) // Default page size
                    .andExpect(jsonPath("$.totalElements").value(numberOfDealers))
                    .andExpect(
                            jsonPath("$.content[0].id").value(dealers.getFirst().getId()))
                    .andExpect(
                            jsonPath("$.content[99].id").value(dealers.get(99).getId()));
        }

        @Test
        public void shouldReturnProducts_withQueryParams() throws Exception {
            mockMvc.perform(get("/api/dealers?size=5&page=1&sort=id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.totalElements").value(numberOfDealers))
                    .andExpect(
                            jsonPath("$.content[0].id").value(dealers.get(194).getId()))
                    .andExpect(
                            jsonPath("$.content[4].id").value(dealers.get(190).getId()));
        }
    }
}
