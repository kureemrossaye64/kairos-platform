package com.kairos.search.model;

public interface VectorSearcheable {
	
	public	float[] getTextEmbedding();
	
	public void setTextEmbedding(float[] embeddings);

}
