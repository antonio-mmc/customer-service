package org.springframework.samples.petclinic.customer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.samples.petclinic.customer.domain.Owner;

import java.util.List;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;
import org.springframework.samples.petclinic.customer.domain.PetType;
import org.springframework.samples.petclinic.customer.dto.OwnerRequest;
import org.springframework.samples.petclinic.customer.dto.PetRequest;
import org.springframework.samples.petclinic.customer.exception.OwnerNotFoundException;
import org.springframework.samples.petclinic.customer.exception.PetNotFoundException;
import org.springframework.samples.petclinic.customer.exception.PetStatusException;
import org.springframework.samples.petclinic.customer.messaging.PetEventPublisher;
import org.springframework.samples.petclinic.customer.repository.OwnerRepository;
import org.springframework.samples.petclinic.customer.repository.PetRepository;
import org.springframework.samples.petclinic.customer.repository.PetTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final PetTypeRepository petTypeRepository;
    private final PetEventPublisher petEventPublisher;

    @Transactional
    public Owner createOwner(OwnerRequest request) {
        return ownerRepository.save(request.toEntity());
    }

    @Transactional(readOnly = true)
    public Owner findOwnerById(Long ownerId) {
        return ownerRepository.findByIdWithPetsAndTypes(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
    }

    @Transactional(readOnly = true)
    public List<Owner> getAllOwners(String lastName) {
        if (lastName != null && !lastName.isBlank()) {
            return ownerRepository.findByLastNameContainingWithPetsAndTypes(lastName);
        }
        return ownerRepository.findAllWithPetsAndTypes();
    }

    @Transactional
    public Owner updateOwner(Long ownerId, OwnerRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
        ownerRepository.save(owner);
        return ownerRepository.findByIdWithPetsAndTypes(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
        ownerRepository.delete(owner);
    }

    @Transactional(readOnly = true)
    public Pet getPetById(Long petId) {
        return petRepository.findByIdWithOwnerAndType(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    @Transactional
    public Pet createPet(Long ownerId, PetRequest dto) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        PetType petType = petTypeRepository.findById(dto.typeId())
                .orElseThrow(() -> new IllegalArgumentException("PetType not found with id: " + dto.typeId()));

        Pet pet = Pet.builder()
                .name(dto.name())
                .birthDate(dto.birthDate())
                .status(PetStatus.ACTIVE)
                .type(petType)
                .owner(owner)
                .build();

        return petRepository.save(pet);
    }

    @Transactional
    public Pet deactivatePet(Long petId) {
        Pet pet = petRepository.findByIdWithOwnerAndType(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (pet.getStatus() == PetStatus.INACTIVE) {
            throw new PetStatusException("Pet with id " + petId + " is already INACTIVE");
        }

        pet.setStatus(PetStatus.INACTIVE);
        Pet saved = petRepository.save(pet);
        petEventPublisher.publishPetDeactivatedEvent(petId);
        return saved;
    }
}
