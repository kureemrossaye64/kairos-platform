package com.kairos.core.ai;



/**
 * Defines the contract for a text embedding model service.
 * This interface decoules our application from the specific provider.
 * Like the ChatLanguageModel, we extend the LangChain4j interface for compatibility.
 */
public interface EmbeddingModel  {
	
	public  dev.langchain4j.model.embedding.EmbeddingModel getModel();
    
}