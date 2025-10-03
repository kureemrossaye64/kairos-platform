package com.kairos.sports_atlas.services;


import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.sports_atlas.entities.ReviewStatus;
import com.kairos.sports_atlas.facility.dto.PendingReviewItem;
import com.kairos.sports_atlas.facility.service.ApprovalService;
import com.kairos.sports_atlas.repositories.ManifestableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final List<ManifestableRepository<? extends Manifestable>> manifestableRepositories;
    private final ApprovalService approvalService;

    /**
     * Gathers all pending review items from all registered manifestable repositories.
     * @return A unified list of PendingReviewItem DTOs.
     */
    @Transactional(readOnly = true)
    public List<PendingReviewItem> getPendingItems() {
        return manifestableRepositories.stream()
            .map(repo -> repo.findByReviewStatus(ReviewStatus.PENDING)) // Call the common method
            .flatMap(Collection::stream) // Flatten the list of lists into a single stream
            .map(this::mapToDto) // Convert each entity to our DTO
            .collect(Collectors.toList());
    }

    private PendingReviewItem mapToDto(Manifestable entity) {
        return new PendingReviewItem(
            entity.getId(),
            entity.getTitle(), // Assuming getTitle() is part of the interface
            entity.getClass().getSimpleName(), // Get the concrete type name
            entity.getCreatedAt().toString() // Assuming getCreatedAt() is on BaseEntity
        );
    }
    
    public void approveItem(String entityType, UUID entityId) {
    	approvalService.approveItem(entityType, entityId);
    }
}