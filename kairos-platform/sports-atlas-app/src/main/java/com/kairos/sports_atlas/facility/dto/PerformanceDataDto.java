package com.kairos.sports_atlas.facility.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * A Data Transfer Object (DTO) representing a single, parsed row
 * from a performance results CSV file.
 */
@Value
@Builder
public class PerformanceDataDto {
    String athleteName;
    LocalDate dateOfBirth;
    String sport;
    String eventName;
    String result;
    String unit;
    LocalDate eventDate;
}