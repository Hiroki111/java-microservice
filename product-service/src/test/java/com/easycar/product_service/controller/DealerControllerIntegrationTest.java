package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.repository.DealerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.sql.init.mode=never"})
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
    class GetProductTests {
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
    @DisplayName("POST /api/dealers")
    class CreateDealerTests {
        @Test
        public void shouldPersistDealer() throws Exception {
            String dealerName = "Alpha Auto";
            DealerCreateDto payload = DealerCreateDto.builder()
                    .name(dealerName)
                    .address("Fairfield 5")
                    .build();

            mockMvc.perform(post("/api/dealers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            Optional<Dealer> saved = dealerRepository.findAll().stream()
                    .filter(dealer -> dealer.getName().equals(dealerName))
                    .findFirst();

            assertThat(saved).isPresent();
            assertThat(saved.get().getName()).isEqualTo(payload.getName());
        }

        @Test
        public void shouldReturnBadRequest_withEmptyName() throws Exception {
            String dealerName = "";
            DealerCreateDto payload = DealerCreateDto.builder()
                    .name(dealerName)
                    .address("Fairfield 5")
                    .build();

            mockMvc.perform(post("/api/dealers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> firstEntity = dealerRepository.findAll().stream().findFirst();

            assertThat(firstEntity).isNotPresent();
        }

        @Test
        public void shouldReturnBadRequest_withEmptyAddress() throws Exception {
            String address = "";
            DealerCreateDto payload = DealerCreateDto.builder()
                    .name("Alpha Auto")
                    .address(address)
                    .build();

            mockMvc.perform(post("/api/dealers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> firstEntity = dealerRepository.findAll().stream().findFirst();

            assertThat(firstEntity).isNotPresent();
        }

        @Test
        public void shouldReturnBadRequest_withDuplicateAddress() throws Exception {
            String address = "Peach street 123";
            dealerRepository.save(
                    Dealer.builder().name("Sunshine Auto").address(address).build());

            String newDealerName = "Alpha Auto";
            DealerCreateDto payload = DealerCreateDto.builder()
                    .name(newDealerName)
                    .address(address)
                    .build();

            mockMvc.perform(post("/api/dealers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> newDealer = dealerRepository.findAll().stream()
                    .filter(dealer -> dealer.getName().equals(newDealerName))
                    .findFirst();

            assertThat(newDealer).isNotPresent();
        }
    }
}
