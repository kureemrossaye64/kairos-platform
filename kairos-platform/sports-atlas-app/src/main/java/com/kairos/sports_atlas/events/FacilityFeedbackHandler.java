package com.kairos.sports_atlas.events;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.agentic_framework.feedback.FeedbackHandler;
import com.kairos.agentic_framework.feedback.entity.PendingFeedbackRequest;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.repositories.FacilityRepository;
import com.kairos.sports_atlas.repositories.ServiceEntityRepository;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class FacilityFeedbackHandler implements FeedbackHandler<BookingCompletedEvent> {

    private final ServiceEntityRepository serviceRepository;
    private final VectorStoreService vectorStoreService;

    @Override
    public String getEntityType() {
        return "Facility";
    }

    @Override
    public boolean canHandle(com.kairos.agentic_framework.feedback.FeedbackTriggerEvent event) {
        return event instanceof BookingCompletedEvent;
    }

    @Override
    public PendingFeedbackRequest createPendingRequest(BookingCompletedEvent event) {
        PendingFeedbackRequest request = new PendingFeedbackRequest();
        request.setEntityType(getEntityType());
        request.setEntityId(event.getEntityId());
        request.setUserId(event.getUserId());
        request.setDispatchAt(LocalDateTime.now().plusHours(24)); // Send request 24 hours after booking
        request.setUniqueToken(UUID.randomUUID().toString()); // Generate a secure, random token
        return request;
    }

    @Override
    @Transactional
    public void processFeedback(UUID entityId, int rating, String comment) {
        ServiceEntity facility = serviceRepository.findById(entityId)
                .orElseThrow(() -> new RuntimeException("Service not found for feedback: " + entityId));

        // 1. Update relational data (e.g., average rating - simplified here)
        // In a real app, you'd have columns for rating_count and rating_sum
        log.info("Updating rating for facility '{}' with new rating: {}", facility.getName(), rating);
        // facility.setAverageRating(...); // Update logic here
        serviceRepository.save(facility);

        // 2. Ingest unstructured comment into the vector store
        if (comment != null && !comment.isBlank()) {
            VdbDocument feedbackDocument = VdbDocument.builder()
                    .id(UUID.randomUUID())
                    .content(comment)
                    .metadata(Map.of(
                            "source", "user_feedback",
                            "entity_type", getEntityType(),
                            "entity_id", facility.getId().toString(),
                            "rating", rating
                    ))
                    .build();
            vectorStoreService.addDocument(feedbackDocument);
            log.info("Indexed feedback comment for facility '{}' in vector store.", facility.getName());
        }
    }
}