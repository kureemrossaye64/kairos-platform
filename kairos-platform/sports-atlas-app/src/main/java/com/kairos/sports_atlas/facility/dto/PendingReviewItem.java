package com.kairos.sports_atlas.facility.dto;

import java.util.UUID;

public record PendingReviewItem(UUID id, String name, String type, String submittedAt) {

}
