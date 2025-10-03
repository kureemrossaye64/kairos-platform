package com.kairos.sports_atlas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.ai_abstraction.service.impl.VertexAiEmbeddingModel;
import com.kairos.ai_abstraction.service.impl.VertexAiGeminiChatModel;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ai.EmbeddingModel;

@Configuration
public class AiConfig {
	
	
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
	
	@Bean
	public ChatLanguageModel chatLanguageModel(VertexAiProperties properties) {
		return new VertexAiGeminiChatModel(properties);
	}

}
