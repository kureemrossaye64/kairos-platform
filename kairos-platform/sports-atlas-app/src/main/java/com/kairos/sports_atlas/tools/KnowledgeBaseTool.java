package com.kairos.sports_atlas.tools;

import java.util.List;

import com.kairos.agentic_framework.tools.KairosTool;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;

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

    @Tool("Searches the platform's comprehensive knowledge base for information on a given topic. Use this to answer general questions, find rules and regulations, get advice, or look up information that is not a specific facility, person, or registered service.")
    public List<VdbDocument> searchKnowledgeBase(
            @P("A detailed, natural language question or query about the topic the user is interested in.") String query
    ) {
        log.info("Knowledge Base Tool: Performing semantic search for query: '{}'", query);

        // 1. Use the vector search capability of our SearchService.
        // We retrieve the top 3-5 most relevant chunks of text.
        List<VdbDocument> relevantChunks = searchService.findRelevant(query, 5);

        if (relevantChunks.isEmpty()) {
            log.warn("Knowledge Base Tool: No relevant documents found for query: '{}'", query);
            return List.of( VdbDocument.builder().content("I could not find any information about that topic in the knowledge base.").build());
            //return "I could not find any information about that topic in the knowledge base.";
        }
        
        return relevantChunks;

        // 2. Format the retrieved chunks into a clear context for the LLM.
        // This is a critical step. We provide the source and the content for each chunk.
        // This allows the LLM to synthesize an answer AND potentially cite its sources.
		/*
		 * String context = relevantChunks.stream() .map(doc -> { String source =
		 * doc.getMetadata().getOrDefault("source_filename",
		 * "Unknown Source").toString(); return
		 * String.format("--- Context from %s ---\n%s\n--- End Context ---", source,
		 * doc.getContent()); }) .collect(Collectors.joining("\n\n"));
		 * 
		 * log.
		 * debug("Knowledge Base Tool: Providing the following context to the LLM:\n{}",
		 * context);
		 * 
		 * // 3. Return the formatted context. The main AI agent will use this to
		 * generate the final answer. // The tool's job is to RETRIEVE, not to generate
		 * the final answer. return
		 * "Based on the knowledge base, here is some relevant information I found:\n" +
		 * context;
		 */
    }
}