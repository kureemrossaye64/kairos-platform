package com.kairos.cultural_archive.controller;

import java.io.ByteArrayInputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kairos.agentic_framework.agent.AgentFactory;
import com.kairos.agentic_framework.agent.AiAgent;
import com.kairos.cultural_archive.controller.dto.ChatRequest;
import com.kairos.cultural_archive.model.AssetType;
import com.kairos.cultural_archive.service.ingestion.IngestionService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final AgentFactory agentFactory;
    private final IngestionService ingestionService;
    private AiAgent archiveAgent;

    // This method creates the agent when the controller is initialized.
    // The AgentFactory will automatically find our @KairosTool (ArchiveSearchTool).
    @PostConstruct
    public void init() {
        this.archiveAgent = agentFactory.createAgent();
    }

    @PostMapping("/chat")
    public String chatWithArchive(@RequestBody ChatRequest request) {
        return archiveAgent.chat(request.message());
    }
    
    /**
     * Endpoint to ingest a new audio file into the cultural archive.
     *
     * @param file The audio file (e.g., mp3, wav).
     * @param title The title of the asset.
     * @param assetType The type of the asset (e.g., TRADITIONAL_SONG).
     * @return An HTTP 202 Accepted response.
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("assetType") AssetType assetType)throws Exception {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty.");
        }
        
        byte[] buffer = file.getBytes();
        ByteArrayInputStream baos = new ByteArrayInputStream(buffer);

        // Trigger the asynchronous ingestion process
        ingestionService.ingestAudioFile(baos,file.getContentType(), file.getOriginalFilename(), title, assetType);

        // Immediately return an 'Accepted' response. The client doesn't wait for processing.
        return ResponseEntity.accepted().body("File upload received. Ingestion process started.");
    }

    // We will add an ingestion endpoint here later
}