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
import org.springframework.samples.petclinic.customer.exception.OwnerNotFoundException;
import org.springframework.samples.petclinic.customer.service.CustomerService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerController.class)
@TestPropertySource(properties = "eureka.client.enabled=false")
@DisplayName("OwnerController REST API Tests")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private Owner sampleOwner;

    @BeforeEach
    void setUp() {
        PetType petType = PetType.builder().id(1L).name("Cat").build();

        Pet pet = Pet.builder()
                .id(1L)
                .name("Whiskers")
                .birthDate(LocalDate.of(2020, 1, 15))
                .type(petType)
                .status(PetStatus.ACTIVE)
                .build();

        sampleOwner = Owner.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .address("110 W. Liberty St.")
                .city("Madison")
                .telephone("6085551023")
                .pets(new ArrayList<>())
                .build();

        sampleOwner.getPets().add(pet);
        pet.setOwner(sampleOwner);
    }

    @Nested
    @DisplayName("GET /api/owners/{ownerId}")
    class GetOwner {

        @Test
        @DisplayName("should return owner with HTTP 200 when found")
        void shouldReturnOwnerWhenFound() throws Exception {
            given(customerService.findOwnerById(1L)).willReturn(sampleOwner);

            mockMvc.perform(get("/api/owners/{ownerId}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")))
                    .andExpect(jsonPath("$.telephone", is("6085551023")))
                    .andExpect(jsonPath("$.pets", hasSize(1)))
                    .andExpect(jsonPath("$.pets[0].name", is("Whiskers")))
                    .andExpect(jsonPath("$.pets[0].status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return HTTP 404 with ProblemDetail when owner not found")
        void shouldReturn404WhenOwnerNotFound() throws Exception {
            given(customerService.findOwnerById(99L))
                    .willThrow(new OwnerNotFoundException(99L));

            mockMvc.perform(get("/api/owners/{ownerId}", 99L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Owner Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail", containsString("99")));
        }
    }

    @Nested
    @DisplayName("POST /api/owners/{ownerId}/pets")
    class CreatePet {

        @Test
        @DisplayName("should create pet with HTTP 201 when owner exists and data is valid")
        void shouldCreatePetWhenOwnerExistsAndDataValid() throws Exception {
            String requestBody = """
                    {
                        "name": "Whiskers",
                        "birthDate": "2020-01-15",
                        "typeId": 1
                    }
                    """;

            PetType petType = PetType.builder().id(1L).name("Cat").build();
            Pet newPet = Pet.builder()
                    .id(2L)
                    .name("Whiskers")
                    .birthDate(LocalDate.of(2020, 1, 15))
                    .type(petType)
                    .status(PetStatus.ACTIVE)
                    .build();

            given(customerService.createPet(eq(1L), any())).willReturn(newPet);

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.name", is("Whiskers")))
                    .andExpect(jsonPath("$.typeName", is("Cat")))
                    .andExpect(jsonPath("$.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return HTTP 404 when owner does not exist")
        void shouldReturn404WhenOwnerNotFound() throws Exception {
            String requestBody = """
                    {
                        "name": "Whiskers",
                        "birthDate": "2020-01-15",
                        "typeId": 1
                    }
                    """;

            given(customerService.createPet(eq(99L), any()))
                    .willThrow(new OwnerNotFoundException(99L));

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Owner Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail", containsString("99")));
        }

        @Test
        @DisplayName("should return HTTP 400 with fieldErrors when request body is invalid")
        void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
            String requestBody = """
                    {
                        "name": "",
                        "birthDate": null,
                        "typeId": null
                    }
                    """;

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Validation Error")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Request validation failed")))
                    .andExpect(jsonPath("$.fieldErrors").exists())
                    .andExpect(jsonPath("$.fieldErrors.name").exists())
                    .andExpect(jsonPath("$.fieldErrors.birthDate").exists())
                    .andExpect(jsonPath("$.fieldErrors.typeId").exists());
        }
    }
}
