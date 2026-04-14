package com.kairos.ingestion.graph.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphView {
    private GraphNodeInfo focusNode;
    private List<GraphNodeInfo> neighbors;
    private List<String> relationshipTypes; // e.g., "CONTAINS", "RELATED_TO"
}