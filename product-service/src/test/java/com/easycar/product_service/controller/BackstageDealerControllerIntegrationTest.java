package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerPatchDto;
import com.easycar.product_service.repository.DealerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class BackstageDealerControllerIntegrationTest {
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
    @DisplayName("POST /api/backstage/dealers")
    class CreateDealerTests {
        @Test
        public void shouldPersistDealer() throws Exception {
            String dealerName = "Alpha Auto";
            DealerCreateDto payload = DealerCreateDto.builder()
                    .name(dealerName)
                    .address("Fairfield 5")
                    .build();

            mockMvc.perform(post("/api/backstage/dealers")
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

            mockMvc.perform(post("/api/backstage/dealers")
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

            mockMvc.perform(post("/api/backstage/dealers")
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

            mockMvc.perform(post("/api/backstage/dealers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> newDealer = dealerRepository.findAll().stream()
                    .filter(dealer -> dealer.getName().equals(newDealerName))
                    .findFirst();

            assertThat(newDealer).isNotPresent();
        }
    }

    @Nested
    @DisplayName("PATCH /api/backstage/dealers/{id}")
    class UpdateDealerTests {
        private Dealer dealer;
        private DealerPatchDto payload;

        @BeforeEach
        void setupData() {
            dealer = dealerRepository.save(Dealer.builder()
                    .name("Ruby Auto")
                    .address("Peach Street 123")
                    .build());

            payload = new DealerPatchDto();
            payload.setName("AAA Auto");
            payload.setAddress("Sunshine Street 456");
        }

        @Test
        public void shouldUpdateDealer() throws Exception {
            mockMvc.perform(patch("/api/backstage/dealers/" + dealer.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk());

            Optional<Dealer> updated = dealerRepository.findById(dealer.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getName()).isEqualTo("AAA Auto");
            assertThat(updated.get().getAddress()).isEqualTo("Sunshine Street 456");
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            long nonexistentId = 999999L;
            mockMvc.perform(patch("/api/backstage/dealers/" + nonexistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnBadRequest_withEmptyName() throws Exception {
            payload.setName("");

            mockMvc.perform(patch("/api/backstage/dealers/" + dealer.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> currentDealer = dealerRepository.findById(dealer.getId());
            assertThat(currentDealer).isPresent();
            assertThat(currentDealer.get().getName()).isEqualTo("Ruby Auto");
            assertThat(currentDealer.get().getAddress()).isEqualTo("Peach Street 123");
        }

        @Test
        public void shouldReturnBadRequest_withEmptyAddress() throws Exception {
            payload.setAddress("");

            mockMvc.perform(patch("/api/backstage/dealers/" + dealer.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> currentDealer = dealerRepository.findById(dealer.getId());
            assertThat(currentDealer).isPresent();
            assertThat(currentDealer.get().getName()).isEqualTo("Ruby Auto");
            assertThat(currentDealer.get().getAddress()).isEqualTo("Peach Street 123");
        }
    }

    @Nested
    @DisplayName("DELETE /api/backstage/dealers/{id}")
    class DeleteDealerTests {
        private Dealer dealer;

        @BeforeEach
        void setupDealerDb() {
            dealer = dealerRepository.save(Dealer.builder()
                    .name("AAA Auto")
                    .address("Peach Street 123")
                    .build());
        }

        @Test
        public void shouldDeleteDealer() throws Exception {
            mockMvc.perform(delete("/api/backstage/dealers/" + dealer.getId()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            Optional<Dealer> updated = dealerRepository.findById(dealer.getId());
            assertThat(updated).isNotPresent();
        }

        @Test
        public void shouldReturnNotFound_withNonExistentId() throws Exception {
            long nonexistentId = 999999L;
            mockMvc.perform(delete("/api/backstage/dealers/" + nonexistentId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
