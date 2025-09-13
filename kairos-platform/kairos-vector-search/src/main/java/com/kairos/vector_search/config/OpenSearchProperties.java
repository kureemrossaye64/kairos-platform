package com.kairos.vector_search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "kairos.vector-store.opensearch")
@Validated
@Getter
@Setter
public class OpenSearchProperties {
	
	@NotBlank
    private String baseUrl;

    @NotBlank
    private String indexName = "kairos-default";

    private int timeoutSeconds = 30;
    
    private String username;
    
    private String password;
    
    private String apiKey;

}
