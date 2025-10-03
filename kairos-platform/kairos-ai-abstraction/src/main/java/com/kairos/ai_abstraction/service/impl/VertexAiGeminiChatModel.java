package com.kairos.ai_abstraction.service.impl;

import java.util.List;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.core.ai.ChatLanguageModel;

/**
 * Concrete implementation of ChatLanguageModel using Google's Vertex AI Gemini.
 * This class is a thin wrapper around LangChain4j's implementation, primarily
 * used for dependency injection and centralized configuration.
 */
public class VertexAiGeminiChatModel extends dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel implements ChatLanguageModel {

    public VertexAiGeminiChatModel(VertexAiProperties props) {
        super(builder()
                .project(props.getProjectId())
                .location(props.getLocation())
                .modelName(props.getChatModelName())
                .maxOutputTokens(props.getMaxOutputTokens())
                .temperature(props.getTemperature().floatValue())
                .topP(props.getTopP().floatValue())
                .topK(props.getTopK())
                //.listeners(List.of(new KairosChatListener()))
        );
    }
}