package com.kairos.cultural_archive.service.ingestion;


import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kairos.ai_abstraction.service.AudioTranscriptionService;
import com.kairos.cultural_archive.entity.CulturalAsset;
import com.kairos.cultural_archive.model.AssetType;
import com.kairos.ingestion.pipeline.DataSource;
import com.kairos.ingestion.pipeline.Pipeline;
import com.kairos.ingestion.pipeline.Processor;
import com.kairos.storage.StorageService;
import com.kairos.storage.gcs.StorageFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for orchestrating the ingestion of new cultural assets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final AudioTranscriptionService transcriptionService;
    private final CulturalAssetSink culturalAssetSink;
    private final StorageService storageService;

    /**
     * Processes an uploaded audio file asynchronously.
     * This method defines and runs the entire ingestion pipeline.
     *
     * @param audioFile The multipart audio file uploaded by the user.
     * @param title The title for the new cultural asset.
     * @param assetType The type of the new cultural asset.
     */
    @Async // Marks this method for asynchronous execution
    public void ingestAudioFile(InputStream audioFile, String contentType, String originalFileName, String title, AssetType assetType)throws Exception {
        
    	
    	String uniqueFileName = UUID.randomUUID() + "-" + originalFileName;
        log.debug("Uploading to GCS with name: {}", uniqueFileName);
        String gcsUri = storageService.upload(audioFile, uniqueFileName, contentType);
        log.info("File uploaded to GCS: {}", gcsUri);
    	
    	
    	//log.info("Starting ingestion process for file: {}", audioFile.getOriginalFilename());

        
       // byte[] buffer = audioFile.getBytes();
       // ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
       // IOUtils.readFully(audioFile.getInputStream(), null);
       // fout.write(null);
        // 1. Define the DataSource
        // The source is the single uploaded audio file's input stream.
        DataSource<StorageFile> audioSource = () -> {
            try {
                return Stream.of(StorageFile.builder().contentType(contentType).name(originalFileName).storageUri(gcsUri).build());
            } catch (Exception e) {
                log.error("Failed to get input stream from audio file", e);
                return Stream.empty();
            }
        };

        // 2. Define the Processor
        // This processor transcribes the audio and maps it to our CulturalAsset entity.
        Processor<StorageFile, CulturalAsset> transcriptionProcessor = file ->
                file.flatMap(f -> {
                    try {
                        log.info("Submitting audio stream for transcription...");
                        CompletableFuture<String> transcriptionFuture = transcriptionService.transcribe( f.getStorageUri());
                        
                        // Wait for the transcription to complete and create the asset
                        String transcript = transcriptionFuture.join();
                        log.info("Transcription received. Length: {} characters.", transcript.length());

                        CulturalAsset asset = new CulturalAsset();
                        asset.setTitle(title);
                        asset.setAssetType(assetType);
                        asset.setDescription(transcript); // The transcript becomes the description
                        asset.setOriginalFileName(originalFileName);
                        
                        return Stream.of(asset);
                    } catch (Exception e) {
                        log.error("Error during transcription processing", e);
                        return Stream.empty();
                    }
                });

        // 3. Build and Run the Pipeline
        Pipeline.from(audioSource)
                .through(transcriptionProcessor)
                .to(culturalAssetSink); // The sink saves to both databases
    }
}