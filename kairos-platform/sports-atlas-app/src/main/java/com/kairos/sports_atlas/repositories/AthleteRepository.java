package com.kairos.sports_atlas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.Athlete;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AthleteRepository extends JpaRepository<Athlete, UUID> {
    // Crucial for ingestion to avoid creating duplicate athletes
    Optional<Athlete> findByNameAndDateOfBirth(String name, LocalDate dateOfBirth);
}