package org.springframework.samples.petclinic.customer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customer.domain.Owner;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;
import org.springframework.samples.petclinic.customer.domain.PetType;
import org.springframework.samples.petclinic.customer.repository.PetRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalPetController.class)
@TestPropertySource(properties = "eureka.client.enabled=false")
@DisplayName("InternalPetController REST API Tests")
class InternalPetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetRepository petRepository;

    private Pet activePet;
    private Pet inactivePet;

    @BeforeEach
    void setUp() {
        PetType petType = PetType.builder().id(1L).name("Cat").build();

        Owner owner = Owner.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .address("110 W. Liberty St.")
                .city("Madison")
                .telephone("6085551023")
                .pets(new ArrayList<>())
                .build();

        activePet = Pet.builder()
                .id(10L)
                .name("Whiskers")
                .birthDate(LocalDate.of(2020, 1, 15))
                .type(petType)
                .owner(owner)
                .status(PetStatus.ACTIVE)
                .build();

        inactivePet = Pet.builder()
                .id(20L)
                .name("OldPet")
                .birthDate(LocalDate.of(2010, 5, 10))
                .type(petType)
                .owner(owner)
                .status(PetStatus.INACTIVE)
                .build();
    }

    @Nested
    @DisplayName("GET /api/internal/pets/{petId}")
    class ValidatePet {

        @Test
        @DisplayName("should return validation response with HTTP 200 when pet is active")
        void shouldReturn200ForActivePet() throws Exception {
            given(petRepository.findByIdWithOwnerAndType(10L)).willReturn(Optional.of(activePet));

            mockMvc.perform(get("/api/internal/pets/{petId}", 10L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(10)))
                    .andExpect(jsonPath("$.ownerId", is(1)))
                    .andExpect(jsonPath("$.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return HTTP 404 with ProblemDetail when pet does not exist")
        void shouldReturn404WhenPetNotFound() throws Exception {
            given(petRepository.findByIdWithOwnerAndType(999L)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/internal/pets/{petId}", 999L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Pet Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail", containsString("999")));
        }

        @Test
        @DisplayName("should return HTTP 409 with ProblemDetail when pet is inactive")
        void shouldReturn409WhenPetIsInactive() throws Exception {
            given(petRepository.findByIdWithOwnerAndType(20L)).willReturn(Optional.of(inactivePet));

            mockMvc.perform(get("/api/internal/pets/{petId}", 20L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title", is("Pet Status Conflict")))
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.detail", containsString("20")));
        }
    }
}
