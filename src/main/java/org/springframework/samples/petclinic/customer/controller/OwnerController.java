package org.springframework.samples.petclinic.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.customer.domain.Owner;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.dto.OwnerRequest;
import org.springframework.samples.petclinic.customer.dto.OwnerResponse;
import org.springframework.samples.petclinic.customer.dto.PetRequest;
import org.springframework.samples.petclinic.customer.dto.PetSummary;
import org.springframework.samples.petclinic.customer.service.CustomerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@Tag(name = "Owners", description = "Owner management endpoints")
public class OwnerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List all owners, optionally filtered by last name")
    public ResponseEntity<List<OwnerResponse>> getOwners(@RequestParam(required = false) String lastName) {
        List<OwnerResponse> owners = customerService.getAllOwners(lastName).stream()
                .map(OwnerResponse::from)
                .toList();
        return ResponseEntity.ok(owners);
    }

    @PutMapping("/{ownerId}")
    @Operation(summary = "Update an existing owner")
    public ResponseEntity<OwnerResponse> updateOwner(@PathVariable Long ownerId, @Valid @RequestBody OwnerRequest ownerRequest) {
        Owner owner = customerService.updateOwner(ownerId, ownerRequest);
        return ResponseEntity.ok(OwnerResponse.from(owner));
    }

    @DeleteMapping("/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an owner")
    public void deleteOwner(@PathVariable Long ownerId) {
        customerService.deleteOwner(ownerId);
    }

    @PostMapping
    @Operation(summary = "Create a new owner")
    public ResponseEntity<OwnerResponse> createOwner(@Valid @RequestBody OwnerRequest ownerRequest) {
        Owner owner = customerService.createOwner(ownerRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{ownerId}")
                .buildAndExpand(owner.getId())
                .toUri();
        return ResponseEntity.created(location).body(OwnerResponse.from(owner));
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get owner by ID including their pets")
    public ResponseEntity<OwnerResponse> getOwner(@PathVariable Long ownerId) {
        Owner owner = customerService.findOwnerById(ownerId);
        return ResponseEntity.ok(OwnerResponse.from(owner));
    }

    @PostMapping("/{ownerId}/pets")
    @Operation(summary = "Add a new pet to an owner")
    public ResponseEntity<PetSummary> createPet(
            @PathVariable Long ownerId,
            @Valid @RequestBody PetRequest petRequest
    ) {
        Pet pet = customerService.createPet(ownerId, petRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(PetSummary.from(pet));
    }
}
