package org.springframework.samples.petclinic.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.customer.domain.Owner;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets p LEFT JOIN FETCH p.type WHERE o.id = :id")
    Optional<Owner> findByIdWithPetsAndTypes(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets p LEFT JOIN FETCH p.type")
    List<Owner> findAllWithPetsAndTypes();

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets p LEFT JOIN FETCH p.type WHERE LOWER(o.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Owner> findByLastNameContainingWithPetsAndTypes(@Param("lastName") String lastName);
}
