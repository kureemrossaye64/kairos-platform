package com.kairos.ingestion.service;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ai.ChatLanguageModel; // Your wrapper
import com.kairos.ingestion.ai.LibrarianBrain;
import com.kairos.ingestion.ai.PlacementDecision;
import com.kairos.ingestion.graph.model.GraphNodeInfo;
import com.kairos.ingestion.graph.model.GraphView;
import com.kairos.ingestion.graph.spi.KnowledgeGraphRepository;
import com.kairos.ingestion.model.DocumentNode;
import com.kairos.ingestion.model.DocumentTree;

import dev.langchain4j.service.AiServices; // LangChain4j Builder
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LibrarianAgent {

    private final KnowledgeGraphRepository graphRepository;
    private final LibrarianBrain librarianBrain;
    private final ObjectMapper objectMapper;

    
    
    // Config: Maximum depth to prevent AI from looping forever
    private static final int MAX_HOPS = 5;

    public LibrarianAgent(KnowledgeGraphRepository graphRepository, 
                          ChatLanguageModel chatModel, 
                          ObjectMapper objectMapper) {
        this.graphRepository = graphRepository;
        this.objectMapper = objectMapper;
        
        // Build the AI Service using LangChain4j
        this.librarianBrain = AiServices.builder(LibrarianBrain.class)
                .chatModel(chatModel.getModel()) // Unwrap your custom wrapper if needed
                .build();
    }

    /**
     * Main Entry Point: Takes a parsed document tree and places it in the graph.
     */
    public void fileDocumentInGraph(DocumentTree documentTree) {
        log.info("Librarian started for document: {}", documentTree.getSourceName());

        // 1. Generate a Summary for the AI Context
        String docContext = extractTextContext(documentTree.getRootNode());
        String summary = librarianBrain.generateSummary(docContext);
        log.info("Document Summary: {}", summary);

        // 2. Start the Graph Traversal Loop
        // We start at the logical ROOT of the graph logic
        String currentNodeId = "ROOT"; 
        String currentLabel = "Universe";
        
        boolean placed = false;
        int hops = 0;

        while (!placed && hops < MAX_HOPS) {
            hops++;
            log.info("--- Hop {}/{} (Current Node: {}) ---", hops, MAX_HOPS, currentLabel);

            // A. Observe: Look at the graph
            List<GraphNodeInfo> candidates;
            if ("ROOT".equals(currentNodeId)) {
                candidates = graphRepository.getRootNodes();
            } else {
                GraphView view = graphRepository.getNodeContext(currentNodeId);
                candidates = view.getNeighbors();
            }

            // B. Orient: Format candidates for AI
            String candidateString = candidates.isEmpty() 
                    ? "(No sub-topics found)" 
                    : candidates.stream()
                        .map(n -> String.format("- [ID: %s] %s (%s)", n.getId(), n.getLabel(), n.getSummary()))
                        .collect(Collectors.joining("\n"));

            // C. Decide: Ask AI what to do
            String jsonResponse = librarianBrain.decidePlacement(summary, currentLabel, candidateString);
            PlacementDecision decision = parseDecision(jsonResponse);
            
            log.info("AI Decision: {} | Reason: {}", decision.getAction(), decision.getReasoning());

            // D. Act: Execute Decision
            placed = executeAction(decision, documentTree, currentNodeId);

            // Update state for next loop if DRILL_DOWN
            if (!placed && "DRILL_DOWN".equalsIgnoreCase(decision.getAction())) {
                currentNodeId = decision.getTargetNodeId();
                // Fetch label for next iteration logging
                // In a real app, optimize to not refetch, but here we keep it safe
                GraphView nextView = graphRepository.getNodeContext(currentNodeId);
                currentLabel = nextView.getFocusNode().getLabel();
            }
        }

        if (!placed) {
            // Fallback: If max hops reached, attach to the last visited node or ROOT
            log.warn("Max hops reached. Attaching to current node: {}", currentNodeId);
            graphRepository.attachDocument(documentTree, currentNodeId, "FALLBACK_LINK");
        }
    }

    private boolean executeAction(PlacementDecision decision, DocumentTree doc, String currentParentId) {
        switch (decision.getAction().toUpperCase()) {
            case "LINK_HERE":
                // Link directly to a specific child node
                if (decision.getTargetNodeId() == null) return false; // Error handling
                graphRepository.attachDocument(doc, decision.getTargetNodeId(), "BELONGS_TO");
                return true;

            case "LINK_TO_CURRENT":
                // The current node we are standing on is the correct place
                graphRepository.attachDocument(doc, currentParentId, "BELONGS_TO");
                return true;

            case "CREATE_TOPIC":
                // Create a new topic under the current parent
                String newTopicId = graphRepository.createTopicNode(
                        decision.getNewTopicName(), 
                        decision.getReasoning(), 
                        currentParentId
                );
                // Then attach the doc to this new topic
                graphRepository.attachDocument(doc, newTopicId, "BELONGS_TO");
                return true;

            case "DRILL_DOWN":
                // Return false so the loop continues with the new target ID
                return false;

            default:
                log.warn("Unknown action: {}", decision.getAction());
                return false;
        }
    }

    // --- Helpers ---

    private PlacementDecision parseDecision(String json) {
        try {
            // Basic cleanup for markdown code blocks often returned by LLMs
            String clean = json.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(clean, PlacementDecision.class);
        } catch (Exception e) {
            log.error("Failed to parse AI JSON decision: {}", json, e);
            // Fallback decision to avoid crash
            PlacementDecision fallback = new PlacementDecision();
            fallback.setAction("LINK_TO_CURRENT");
            fallback.setReasoning("JSON Parsing Failed");
            return fallback;
        }
    }

    private String extractTextContext(DocumentNode node) {
        // Simple BFS/DFS to get first ~2000 chars of text for summary
        StringBuilder sb = new StringBuilder();
        collectText(node, sb);
        return sb.length() > 2000 ? sb.substring(0, 2000) : sb.toString();
    }

    private void collectText(DocumentNode node, StringBuilder sb) {
        if (sb.length() > 2000) return;
        if (node.getTitleOrContent() != null) {
            sb.append(node.getTitleOrContent()).append("\n");
        }
        if (node.getChildren() != null) {
            for (DocumentNode child : node.getChildren()) {
                collectText(child, sb);
            }
        }
    }
}