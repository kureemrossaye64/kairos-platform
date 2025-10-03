package com.kairos.sports_atlas.tools;

import java.util.List;

import com.kairos.agentic.tools.KairosTool;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorStoreService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A specialized tool for performing semantic searches on the platform's
 * ingested knowledge base. This is the primary tool for RAG (Retrieval-Augmented Generation).
 */
@KairosTool
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseTool {

    private final VectorStoreService searchService;

    @Tool("Searches the platform's comprehensive knowledge base for information on a given topic. Use this to answer general questions, find rules and regulations, get advice, or look up information that is not a specific facility, person, or registered service. This tool should also be used if no information were found using the other specialized tools")
    public List<VdbDocument> searchKnowledgeBase(
            @P("A detailed, natural language question or query about the topic the user is interested in.") String query
    ) {
        log.info("Knowledge Base Tool: Performing semantic search for query: '{}'", query);

        List<VdbDocument> relevantChunks = searchService.findRelevant(query, 5);

        if (relevantChunks.isEmpty()) {
            log.warn("Knowledge Base Tool: No relevant documents found for query: '{}'", query);
            return List.of( VdbDocument.builder().content("I could not find any information about that topic in the knowledge base.").build());
        }
        
        return relevantChunks;

       
    }
}