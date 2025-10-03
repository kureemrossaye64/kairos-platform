package com.kairos.sports_atlas.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ingestion.IngestionRequest;
import com.kairos.crawler.models.MockMultipartFile;
import com.kairos.ingestion.rag.IngestionRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIngestionController {

    private final IngestionRouter ingestionRouter;
    private final ObjectMapper objectMapper; // Spring Boot automatically provides this bean.

    /**
     * Accepts a "Knowledge Packet" for asynchronous ingestion into the RAG knowledge base.
     * The request must be of type multipart/form-data.
     *
     * @param file The raw file payload (e.g., a PDF, TXT, DOCX, MP3, MP4). This part is required.
     * @param manifestJson An optional JSON string containing structured metadata for the file.
     *                     Example: '{"activity": "Football", "authority": "MFA", "year": "2025"}'
     * @return An HTTP 202 Accepted response upon successful queuing of the ingestion task.
     */
    @PostMapping("/ingest")
   // @PreAuthorize("hasRole('ROLE_ADMIN')") // Secures the endpoint, only users with ROLE_ADMIN can access.
    public ResponseEntity<String> ingestDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "manifest", required = false) String manifestJson
    ) throws IOException{
        log.info("Received knowledge ingestion request for file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Ingestion request rejected: File part was empty.");
            return ResponseEntity.badRequest().body("The 'file' part of the request cannot be empty.");
        }

        Map<String, Object> metadataManifest;
        try {
            // Safely parse the manifest JSON string if it was provided.
            if (manifestJson != null && !manifestJson.isBlank()) {
                // We use a TypeReference to correctly deserialize the JSON into a Map.
                metadataManifest = objectMapper.readValue(manifestJson, new TypeReference<>() {});
            } else {
                metadataManifest = Collections.emptyMap();
            }
            
            // Use the factory method to create our rich IngestionRequest DTO.
            // This reads the file into a byte array, making it ready for the async pipeline.
            MockMultipartFile mfile = new MockMultipartFile(file.getOriginalFilename(),file.getContentType(),file.getInputStream());
            IngestionRequest request = IngestionRequest.from(mfile, metadataManifest);
            
            // Delegate the heavy lifting to the IngestionRouter, which runs asynchronously.
            ingestionRouter.addRequest(request);

        } catch (JsonProcessingException e) {
            log.error("Ingestion request failed: Manifest JSON is malformed. JSON: {}", manifestJson, e);
            return ResponseEntity.badRequest().body("The provided 'manifest' is not valid JSON.");
        } catch (Exception e) {
            log.error("Failed to initiate ingestion process for file: {}", file.getOriginalFilename(), e);
            // Return a generic error to the client for security.
            return ResponseEntity.internalServerError().body("An unexpected error occurred while processing the request.");
        }
        
        // Return HTTP 202 Accepted. This tells the client that we've received the request
        // and will process it, but the work is not yet complete. This is the correct
        // response for a long-running, asynchronous task.
        return ResponseEntity.accepted().body("Knowledge Packet accepted. Ingestion will be processed in the background.");
    }
}