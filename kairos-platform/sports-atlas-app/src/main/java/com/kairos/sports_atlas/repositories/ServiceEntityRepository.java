package com.kairos.sports_atlas.repositories;


import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.ServiceEntity;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, UUID> {
    // This will be useful for updates: find the manifest by its source.
    Optional<ServiceEntity> findByOriginEntityId(UUID originEntityId);
    
    
}