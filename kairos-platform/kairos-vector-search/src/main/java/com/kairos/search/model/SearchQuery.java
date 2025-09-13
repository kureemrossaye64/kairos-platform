package com.kairos.search.model;

import lombok.Builder;
import org.locationtech.jts.geom.Point;
import java.util.Map;

/**
 * A builder-style DTO for constructing complex, multi-faceted search queries.
 */
@Builder
public record SearchQuery(
    String textQuery,
    Point location,
    Double radiusInKm,
    Map<String, String> keywordFilters
) {}