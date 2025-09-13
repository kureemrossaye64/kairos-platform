package com.kairos.sports_atlas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.TrainingOpportunity;

public interface TrainingOpportunityRepository extends JpaRepository<TrainingOpportunity, UUID> {
    // We don't need custom queries here because all searching will be handled by OpenSearch.
}