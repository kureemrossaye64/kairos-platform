package com.kairos.ai_abstraction.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * A type-safe configuration class to hold all properties related to Google Vertex AI.
 * This centralizes configuration and provides validation.
 * The prefix "kairos.ai.vertex" will be used in application.yml.
 */
@Data
@Builder
public class VertexAiProperties {
	
	@Builder.Default
	private String transcriptionProvider = "speech-to-text";

    @NotBlank
    private String projectId;

    @NotBlank
    private String location;

    @NotBlank
    @Builder.Default
    private String chatModelName = "gemini-2.0-flash";
    
    @NotBlank
    @Builder.Default
    private String transcriptionModelName = "gemini-2.0-flash";

    //@NotBlank
    //@Builder.Default
    //private String embeddingModelName = "textembedding-gecko@003";

    @Builder.Default
    private Integer maxOutputTokens = 1024;
    
    @Builder.Default
    private Double temperature = 0.4;
    
    @Builder.Default
    private Double topP = 0.95;
    
    @Builder.Default
    private Integer topK = 40;
}