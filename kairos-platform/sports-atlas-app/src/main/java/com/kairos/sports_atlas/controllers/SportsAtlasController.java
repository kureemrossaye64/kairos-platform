package com.kairos.sports_atlas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kairos.agentic_framework.agent.AgentFactory;
import com.kairos.agentic_framework.agent.AiAgent;
import com.kairos.sports_atlas.common.SystemPromptService;
import com.kairos.sports_atlas.logging.service.ChatLogService;
import com.kairos.sports_atlas.service.ingestion.SportsIngestionService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.kairos.sports_atlas.util.Util.getConversationId;
record ChatRequest(String message) {}

@RestController
@RequestMapping("/api/v1/atlas")
@RequiredArgsConstructor
@Slf4j
public class SportsAtlasController {

    private final AgentFactory agentFactory;
    private final SportsIngestionService ingestionService;
    private final SystemPromptService systemPromptService;
    private final ChatLogService chatLogService;
    private AiAgent atlasAgent;

    @PostConstruct
    public void init() {
    	String kayaPrompt = systemPromptService.getKayaSystemPrompt();
     	this.atlasAgent = agentFactory.createAgent(kayaPrompt);
    }

    @PostMapping("/chat")
    public String chatWithAtlas(@RequestBody ChatRequest request) {
        String agentResponse = atlasAgent.chat(request.message());
        try {
            chatLogService.logConversation(getConversationId(), request.message(), agentResponse);
        } catch (Exception e) {
            log.error("Failed to log conversation for user {}", getConversationId(), e);
        }
        return agentResponse;
    }

    /**
     * Endpoint to ingest a CSV file of performance data.
     * @param file The CSV file.
     * @return An HTTP 202 Accepted response.
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestPerformanceData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty.");
        }

        ingestionService.ingestPerformanceData(file);

        return ResponseEntity.accepted().body("CSV file received. Ingestion process started.");
    }
}