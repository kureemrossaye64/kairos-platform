package com.kairos.sports_atlas.facility.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingRequestDto(
    UUID facilityId,
    UUID userId, // In a real app, this would come from the authenticated Principal
    LocalDateTime startTime,
    LocalDateTime endTime
) {}