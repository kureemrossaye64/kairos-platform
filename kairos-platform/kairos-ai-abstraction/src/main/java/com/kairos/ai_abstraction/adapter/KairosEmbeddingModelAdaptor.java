package com.kairos.ai_abstraction.adapter;

import com.kairos.core.ai.EmbeddingModel;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KairosEmbeddingModelAdaptor implements EmbeddingModel{
	
	private final dev.langchain4j.model.embedding.EmbeddingModel delegate;

	@Override
	public dev.langchain4j.model.embedding.EmbeddingModel getModel() {
		return delegate;
	}

	
	
	

}
