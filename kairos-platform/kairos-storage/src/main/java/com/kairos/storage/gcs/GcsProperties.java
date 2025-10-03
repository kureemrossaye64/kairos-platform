package com.kairos.storage.gcs;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GcsProperties {
    @NotBlank
    private String bucketName;
    
    private String projectId;
}