package com.kairos.core.search;

import java.util.UUID;

public interface VectorSearcheable {
	
	public UUID getId();
	
	public	float[] getTextEmbedding();
	
	public void setTextEmbedding(float[] embeddings);

}
