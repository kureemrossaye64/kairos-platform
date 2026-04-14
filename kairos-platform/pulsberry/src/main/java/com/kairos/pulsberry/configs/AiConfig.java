package com.kairos.pulsberry.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.ai_abstraction.service.impl.VertexAiEmbeddingModel;
import com.kairos.ai_abstraction.service.impl.VertexAiGeminiChatModel;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ai.EmbeddingModel;

@Configuration
public class AiConfig {
	
	
	@Value("${kairos.ai.vertex.chat-model-name-cost-effective}")
	private String costEffectiveModel;
	
	@Bean
	public VertexAiProperties vertexAIProperties(
			@Value("${kairos.ai.vertex.project-id}") String projectId, 
			@Value("${kairos.ai.vertex.location}") String location, 
			@Value("${kairos.ai.vertex.chat-model-name}") String modelName) {
		return VertexAiProperties.builder().chatModelName(modelName).location(location).projectId(projectId).build();
	}
	
	@Bean
	public EmbeddingModel embeddingModel(VertexAiProperties properties) {
		return new VertexAiEmbeddingModel(properties);
	}
	
	@Bean("pro")
    @Primary
	public ChatLanguageModel chatLanguageModel(VertexAiProperties properties) {
		return new VertexAiGeminiChatModel(properties);
	}
	
	@Bean("cost-effective")
    public ChatLanguageModel geminiFlash(VertexAiProperties props) {
        return new VertexAiGeminiChatModel(VertexAiProperties.builder()
                .projectId(props.getProjectId())
                .location(props.getLocation())
                .chatModelName(costEffectiveModel) // Cheap & Fast
                .temperature(0.0)
                .build());
    }


}
