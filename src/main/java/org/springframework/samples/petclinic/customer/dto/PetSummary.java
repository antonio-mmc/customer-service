package org.springframework.samples.petclinic.customer.dto;

import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;

import java.time.LocalDate;

public record PetSummary(
        Long id,
        Long ownerId,
        String name,
        LocalDate birthDate,
        PetTypeInfo type,
        PetStatus status
) {

    public record PetTypeInfo(Long id, String name) {}

    public static PetSummary from(Pet pet) {
        return new PetSummary(
                pet.getId(),
                pet.getOwner().getId(),
                pet.getName(),
                pet.getBirthDate(),
                new PetTypeInfo(pet.getType().getId(), pet.getType().getName()),
                pet.getStatus()
        );
    }
}
