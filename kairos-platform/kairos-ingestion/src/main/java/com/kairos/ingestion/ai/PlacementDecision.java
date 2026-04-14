package com.kairos.ingestion.ai;

import lombok.Data;

@Data
public class PlacementDecision {
    private String action;       // LINK_HERE, DRILL_DOWN, CREATE_TOPIC, LINK_TO_CURRENT
    private String targetNodeId;
    private String newTopicName;
    private String reasoning;
}