package com.kairos.ingestion.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kairos.ai_abstraction.service.impl.LLMAudioAnalysisService;
import com.kairos.ai_abstraction.service.impl.LLMImageAnalysisService;
import com.kairos.ai_abstraction.service.impl.LLMTextAnalysisService;
import com.kairos.ai_abstraction.service.impl.LLMVideoAnalysisService;
import com.kairos.core.ai.AudioAnalysisService;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ai.ImageAnalysisService;
import com.kairos.core.ai.TextAnalysisService;
import com.kairos.core.ai.VideoAnalysisService;

@Configuration
public class AnalysisServiceConfig {
	
	@Bean
	public AudioAnalysisService audioTranscription(ChatLanguageModel model) {
		return new LLMAudioAnalysisService(model);
	}
	
	@Bean
	public ImageAnalysisService imageAnalysis(ChatLanguageModel model) {
		return new LLMImageAnalysisService(model);
	}
	
	@Bean
	public TextAnalysisService textAnalysis(ChatLanguageModel model) {
		return new LLMTextAnalysisService(model);
	}
	
	@Bean
	public VideoAnalysisService videoAnalysis(ChatLanguageModel model) {
		return new LLMVideoAnalysisService(model);
	}

}
