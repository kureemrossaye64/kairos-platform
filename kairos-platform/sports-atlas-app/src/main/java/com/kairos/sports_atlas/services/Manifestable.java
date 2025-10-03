package com.kairos.sports_atlas.services;

import java.time.Instant;
import java.util.UUID;

import com.kairos.sports_atlas.entities.ReviewStatus;
import com.kairos.sports_atlas.entities.ServiceEntity;

import jakarta.persistence.MappedSuperclass;

/**
 * An interface for domain entities that can be published as a public-facing ServiceEntity.
 */
@MappedSuperclass
public interface Manifestable {
    /**
     * Creates a ServiceEntity manifest from this domain object.
     * @return a fully populated ServiceEntity.
     */
    ServiceEntity toServiceEntity();
    
    public UUID getId();
    
    
    public ReviewStatus getReviewStatus();
    
    public void setReviewStatus(ReviewStatus status);
    
    
    public String getTitle();
    
    public Instant getCreatedAt();
    
    
}