package com.kairos.vector_search.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration for connecting to a ChromaDB instance.
 * The prefix "kairos.vector-store.chroma" will be used in application.yml.
 */
@ConfigurationProperties(prefix = "kairos.vector-store.chroma")
@Validated
@Getter
@Setter
public class ChromaProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String defaultCollectionName = "kairos-default";

    private int timeoutSeconds = 30;
}