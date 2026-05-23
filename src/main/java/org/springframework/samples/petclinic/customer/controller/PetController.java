package org.springframework.samples.petclinic.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.dto.PetSummary;
import org.springframework.samples.petclinic.customer.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Pet management endpoints")
public class PetController {

    private final CustomerService customerService;

    @GetMapping("/{petId}")
    @Operation(summary = "Get a pet by ID")
    public ResponseEntity<PetSummary> getPetById(@PathVariable Long petId) {
        Pet pet = customerService.getPetById(petId);
        return ResponseEntity.ok(PetSummary.from(pet));
    }

    @PatchMapping("/{petId}/deactivate")
    @Operation(summary = "Deactivate a pet by ID")
    public ResponseEntity<PetSummary> deactivatePet(@PathVariable Long petId) {
        Pet pet = customerService.deactivatePet(petId);
        return ResponseEntity.ok(PetSummary.from(pet));
    }
}
