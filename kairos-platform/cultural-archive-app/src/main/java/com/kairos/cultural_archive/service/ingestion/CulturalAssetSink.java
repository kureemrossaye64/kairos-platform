package com.kairos.cultural_archive.service.ingestion;

import com.kairos.cultural_archive.entity.CulturalAsset;
import com.kairos.cultural_archive.repository.CulturalAssetRepository;
import com.kairos.ingestion.pipeline.Sink;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The final step (Sink) in our ingestion pipeline.
 * This component takes a stream of transcribed CulturalAsset objects, saves their
 * metadata to the relational database (PostgreSQL), and indexes their transcribed
 * content in the vector store (ChromaDB).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CulturalAssetSink implements Sink<CulturalAsset> {

    private final CulturalAssetRepository culturalAssetRepository;
    private final VectorStoreService vectorStoreService;

    @Override
    public void consume(Stream<CulturalAsset> stream) {
        stream.forEach(asset -> {
            log.info("Sinking asset '{}' into data stores.", asset.getTitle());

            // 1. Save the metadata to the relational database
            CulturalAsset savedAsset = culturalAssetRepository.save(asset);
            log.debug("Saved asset metadata with ID: {}", savedAsset.getId());

            // 2. Create a document for the vector store
            VdbDocument vdbDocument = VdbDocument.builder()
                    .id(UUID.randomUUID()) // A new UUID for the vector store entry
                    .content(asset.getDescription()) // The transcribed text
                    .metadata(Map.of(
                            "asset_id", savedAsset.getId().toString(),
                            "title", savedAsset.getTitle(),
                            "asset_type", savedAsset.getAssetType().toString()
                    ))
                    .build();

            // 3. Add the document to the vector store
            vectorStoreService.addDocument(vdbDocument);
            log.debug("Indexed asset content in vector store. VDB ID: {}", vdbDocument.getId());
            
            // 4. Link the relational entity to the vector store ID
            // In a real system, you might store a list of vector IDs if the text is chunked.
            // For simplicity, we store the first one.
            savedAsset.setVectorStoreId(vdbDocument.getId().toString());
            culturalAssetRepository.save(savedAsset);
        });
    }
}