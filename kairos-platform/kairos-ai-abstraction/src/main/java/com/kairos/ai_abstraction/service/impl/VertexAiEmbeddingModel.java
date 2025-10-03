package com.kairos.ai_abstraction.service.impl;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.core.ai.EmbeddingModel;

import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;

/**
 * Concrete implementation of EmbeddingModel using Google's Vertex AI embedding models.
 */
public class VertexAiEmbeddingModel extends BgeSmallEnV15QuantizedEmbeddingModel implements EmbeddingModel {

    public VertexAiEmbeddingModel(VertexAiProperties props) {
        super();
    }
}