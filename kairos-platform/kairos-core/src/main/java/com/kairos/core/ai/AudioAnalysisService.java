package com.kairos.core.ai;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract for an audio transcription service.
 * This is a custom interface as a direct equivalent is not central to LangChain4j.
 */
public interface AudioAnalysisService {

    /**
     * Transcribes a long audio file asynchronously.
     *
     * @param audioStream The InputStream of the audio data.
     * @param audioMimeType The MIME type of the audio, e.g., "audio/wav".
     * @return A CompletableFuture that will complete with the transcribed text.
     */
    CompletableFuture<String> transcribe(String storageUri);
}