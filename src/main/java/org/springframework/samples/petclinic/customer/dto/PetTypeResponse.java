package org.springframework.samples.petclinic.customer.dto;

import org.springframework.samples.petclinic.customer.domain.PetType;

public record PetTypeResponse(Long id, String name) {

    public static PetTypeResponse from(PetType petType) {
        return new PetTypeResponse(petType.getId(), petType.getName());
    }
}
