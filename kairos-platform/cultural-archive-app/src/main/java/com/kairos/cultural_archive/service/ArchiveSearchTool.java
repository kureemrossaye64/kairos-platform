package com.kairos.cultural_archive.service;

import com.kairos.agentic_framework.tools.KairosTool;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@KairosTool // This makes it a Spring bean and discoverable by our AgentFactory
@RequiredArgsConstructor
@Slf4j
public class ArchiveSearchTool {

    private final VectorStoreService vectorStoreService;

    @Tool("Searches the cultural archive for information on a specific topic. Use this to answer questions about Creole history, stories, songs, or traditions.")
    public String searchArchive(String query) {
        log.info("AI Agent is searching the archive with query: '{}'", query);
        List<VdbDocument> relevantDocs = vectorStoreService.findRelevant(query, 3);

        if (relevantDocs.isEmpty()) {
            return "I could not find any information about that in the archive.";
        }

        // Format the results for the LLM to process
        return relevantDocs.stream()
                .map(doc -> "Found document snippet:\n---\n" + doc.getContent() + "\n---")
                .collect(Collectors.joining("\n\n"));
    }
}