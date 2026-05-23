package org.springframework.samples.petclinic.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.samples.petclinic.customer.dto.PetTypeResponse;
import org.springframework.samples.petclinic.customer.repository.PetTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/petTypes")
@RequiredArgsConstructor
@Tag(name = "PetTypes", description = "Pet type reference data")
public class PetTypeController {

    private final PetTypeRepository petTypeRepository;

    @GetMapping
    @Operation(summary = "List all pet types")
    public List<PetTypeResponse> getAllPetTypes() {
        return petTypeRepository.findAll().stream()
                .map(PetTypeResponse::from)
                .toList();
    }
}
