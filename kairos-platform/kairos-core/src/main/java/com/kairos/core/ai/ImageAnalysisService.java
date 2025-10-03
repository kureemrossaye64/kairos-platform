package com.kairos.core.ai;

import java.util.concurrent.CompletableFuture;

public interface ImageAnalysisService {
	
	
	public CompletableFuture<String> analyze(String storageURI);

}
