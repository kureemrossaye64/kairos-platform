package com.kairos.ingestion.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A lightweight representation of a node for the AI to consume.
 * It does NOT contain the heavy content (text/images).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeInfo {
    private String id;
    private String label;      // e.g., "Project Apollo", "Q3 Financials"
    private String type;       // "TOPIC", "DOCUMENT", "SECTION"
    private String summary;    // Short context for the AI
    private int level;         // Hierarchy level
}

