package com.easycar.product_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.dto.DealerCreateDto;
import com.easycar.product_service.dto.DealerPatchDto;
import com.easycar.product_service.repository.DealerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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

    @Nested
    @DisplayName("PATCH /api/dealers/{id}")
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
            mockMvc.perform(patch("/api/dealers/" + dealer.getId())
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
            mockMvc.perform(patch("/api/dealers/" + nonexistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnBadRequest_withEmptyName() throws Exception {
            payload.setName("");

            mockMvc.perform(patch("/api/dealers/" + dealer.getId())
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

            mockMvc.perform(patch("/api/dealers/" + dealer.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());

            Optional<Dealer> currentDealer = dealerRepository.findById(dealer.getId());
            assertThat(currentDealer).isPresent();
            assertThat(currentDealer.get().getName()).isEqualTo("Ruby Auto");
            assertThat(currentDealer.get().getAddress()).isEqualTo("Peach Street 123");
        }
    }
}
