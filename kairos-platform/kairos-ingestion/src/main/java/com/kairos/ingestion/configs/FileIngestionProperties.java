package com.kairos.ingestion.configs;

import lombok.Data;

@Data
public class FileIngestionProperties {
    private boolean enabled =true;
    private String inputDir = "./ingestion-inbox";
    private String processedDir = "./ingestion-processed";
    private long pollIntervalMs = 5000;
}