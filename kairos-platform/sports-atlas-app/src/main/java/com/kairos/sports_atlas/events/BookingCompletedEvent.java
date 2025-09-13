package com.kairos.sports_atlas.events;

import com.kairos.agentic_framework.feedback.FeedbackTriggerEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class BookingCompletedEvent implements FeedbackTriggerEvent {
    private final UUID entityId; // This is the Facility ID
    private final UUID userId;

    public BookingCompletedEvent(UUID facilityId, UUID userId) {
        this.entityId = facilityId;
        this.userId = userId;
    }
}