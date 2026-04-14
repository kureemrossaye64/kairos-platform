// --- START OF FILE src/main/java/com/kairos/agentic/controller/AssistantController.java ---
package com.kairos.pulsberry.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kairos.agentic.agent.AgentFactory;
import com.kairos.agentic.agent.AiAgent;
import com.kairos.pulsberry.dto.ChatMessageEntityDto;
import com.kairos.pulsberry.dto.ChatSessionDto;
import com.kairos.pulsberry.services.ChatSessionMessageService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AgentFactory agentFactory;
    
    private final ChatSessionMessageService chatService;
    
    
    // Assume standard JPA Repo
    // private final ChatSessionRepository sessionRepo; 

    private static final String SYSTEM_PROMPT = """
        You are an expert Food Safety and Hygiene Assistant.
        You have access to internal manuals (searchComplianceManuals) and live camera feeds.
        IMPORTANT: Every information you provide concerning food safety and compliance should come from the searchComplianceManuals tool
        Provide your own knowledge only and only if there is no relevant information in the searchComplianceManuals and you should politely mention that the you did not find relevant information from the manual and this is your own knowledge
        Always verify regulations using the manuals. Do not guess specific temperatures or codes.
        """;

    @PostMapping("/chat/{sessionId}")
    public String chat(@PathVariable String sessionId, @RequestBody String userMessage) {
        // 1. Create Agent with specific Session ID (links to DB memory)
        // Note: You must update AgentFactory to accept sessionId and use PersistentChatMemoryStore
        AiAgent agent = agentFactory.createAgent(SYSTEM_PROMPT, sessionId);
        
        // 2. Chat (History saved automatically by Store)
        return agent.chat(userMessage);
    }
    
 // --- Session Management ---
    @PostMapping("/session")
    public ResponseEntity<ChatSessionDto> createSession(@RequestBody CreateSessionRequest req) {
        ChatSessionDto result = chatService.createSession(req.getTitle());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDto>> getUserSessions() {
        return ResponseEntity.ok(chatService.getUserSessions());
    }

    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageEntityDto>> getSessionMessages(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatService.getSessionMessages(sessionId));
    }

    // --- DTOs ---
    @Data
    public static class ChatRequest { private String message; }
    @Data
    public static class ChatResponse { 
        private String response; 
        public ChatResponse(String r) { this.response = r; }
    }
    @Data
    public static class CreateSessionRequest { private String title; }
}
// --- END OF FILE ---