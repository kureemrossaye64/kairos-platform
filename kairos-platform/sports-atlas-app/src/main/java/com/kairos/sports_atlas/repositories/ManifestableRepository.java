package com.kairos.sports_atlas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.kairos.sports_atlas.entities.ReviewStatus;
import com.kairos.sports_atlas.services.Manifestable;

@NoRepositoryBean
public interface ManifestableRepository<T extends Manifestable> extends JpaRepository<T, UUID> {
    
    List<T> findByReviewStatus(ReviewStatus status);
}
