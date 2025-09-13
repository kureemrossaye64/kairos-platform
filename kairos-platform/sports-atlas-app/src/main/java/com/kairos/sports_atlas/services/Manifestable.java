package com.kairos.sports_atlas.services;

import java.util.UUID;

import com.kairos.sports_atlas.entities.ServiceEntity;

/**
 * An interface for domain entities that can be published as a public-facing ServiceEntity.
 */
public interface Manifestable {
    /**
     * Creates a ServiceEntity manifest from this domain object.
     * @return a fully populated ServiceEntity.
     */
    ServiceEntity toServiceEntity();
    
    public UUID getId();
    
    
}