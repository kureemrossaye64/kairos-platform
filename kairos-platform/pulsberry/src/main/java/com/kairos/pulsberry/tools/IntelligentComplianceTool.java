// --- START OF FILE src/main/java/com/kairos/agentic/tools/knowledge/IntelligentComplianceTool.java ---
package com.kairos.pulsberry.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kairos.agentic.rag.QueryExpansionService;
import com.kairos.agentic.rag.ResultRankingService;
import com.kairos.agentic.tools.KairosTool;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorStoreService;
import com.kairos.search.postgres.PgVectorStoreService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@KairosTool
@RequiredArgsConstructor
@Slf4j
public class IntelligentComplianceTool {

    private final VectorStoreService vectorStore;
    private final QueryExpansionService queryExpansionService;
    
    private final ResultRankingService rankingService;
    

    @Tool("Searches food safety compliance manuals. Use for regulations, HACCP, hygiene, or temperatures.")
    public String searchComplianceManuals(@P("The user's question") String query) {
    	
    	try {
        String technicalInstruction = "echnical compliance query (HACCP/ISO terms)";
        // 1. Expand Query (Cheap)
        List<String> queries = queryExpansionService.expandQuery(query, technicalInstruction);
        
        // 2. Hybrid Retrieval (Parallel)
        // We use a custom VdbDocument class that corresponds to the 'embeddings' table
        // We assume there is a generic entity class representing the VDB or pass null to use raw logic
        // In this implementation, we modified PgVectorStoreService to take a class. 
        // Let's assume a generic `DocumentationChunk.class` exists in core.
        Map<String, VdbDocument> uniqueDocs = new HashMap<>();
        
        for (String q : queries) {
            // Find top 5 for each variation using Hybrid Search
            // Note: You need to create a dummy class 'DocumentationEntity' annotated with @Searchable 
            // or modify the service to accept a table name string.
            List<VdbDocument> hits = vectorStore.findHybrid(q, 5, DocumentationEntity.class);
            hits.forEach(d -> uniqueDocs.put(d.getId().toString(), d));
        }
        
        List<VdbDocument> candidates = new ArrayList<>(uniqueDocs.values());
        if (candidates.isEmpty()) return "No documentation found.";

        // 3. Re-ranking (Gemini Flash)
        List<VdbDocument> ranked = rankingService.reRankResults(query, candidates);
        
        // 4. Assemble Context
        StringBuilder sb = new StringBuilder("Compliance Manual Excerpts:\n");
        for (VdbDocument doc : ranked) {
            sb.append("--- Source: ").append(doc.getMetadata().get("source_filename")).append(" ---\n");
            sb.append(doc.getContent()).append("\n\n");
        }
        
        return sb.toString();
    	}catch(Exception e) {
    		e.printStackTrace();
    		return "There was an error in executing this tool. Can you please try again later?";
    	}
    }

    
    
    // Internal marker class for the DB mapping
    @com.kairos.core.search.Searchable(indexName = "embeddings")
    public static class DocumentationEntity {}
}
// --- END OF FILE ---