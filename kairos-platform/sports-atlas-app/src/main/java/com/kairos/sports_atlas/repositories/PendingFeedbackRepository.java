package com.kairos.sports_atlas.repositories;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.PendingFeedbackRequest;

public interface PendingFeedbackRepository extends JpaRepository<PendingFeedbackRequest, UUID> {
    List<PendingFeedbackRequest> findByIsDispatchedFalseAndDispatchAtBefore(LocalDateTime now);
    Optional<PendingFeedbackRequest> findByUniqueToken(String token);
}