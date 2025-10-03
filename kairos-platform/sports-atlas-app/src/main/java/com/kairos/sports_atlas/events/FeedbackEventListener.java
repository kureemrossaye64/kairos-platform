package com.kairos.sports_atlas.events;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.kairos.sports_atlas.entities.PendingFeedbackRequest;
import com.kairos.sports_atlas.feedback.FeedbackHandler;
import com.kairos.sports_atlas.feedback.FeedbackTriggerEvent;
import com.kairos.sports_atlas.repositories.PendingFeedbackRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeedbackEventListener {
    private final List<FeedbackHandler> handlers; // Spring injects all implementations
    private final PendingFeedbackRepository pendingRepo;

    @EventListener
    public void handleFeedbackTrigger(FeedbackTriggerEvent event) {
        log.info("Received feedback trigger event for entity ID: {}", event.getEntityId());
        handlers.stream()
            .filter(h -> h.canHandle(event))
            .findFirst()
            .ifPresent(handler -> {
                @SuppressWarnings("unchecked") // Safe cast due to canHandle check
                PendingFeedbackRequest request = handler.createPendingRequest(event);
                pendingRepo.save(request);
                log.info("Created pending feedback request with token: {}", request.getUniqueToken());
            });
    }
}