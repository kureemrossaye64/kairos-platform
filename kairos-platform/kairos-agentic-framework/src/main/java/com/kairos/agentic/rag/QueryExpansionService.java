package com.kairos.agentic.rag;

import java.util.Arrays;
import java.util.List;

import com.kairos.core.ai.ChatLanguageModel;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueryExpansionService {

    private final ChatLanguageModel fastModel;

    
//Technical compliance query (HACCP/ISO terms)
    public List<String> expandQuery(String originalQuery, String technicalInstructions) {
        String prompt = """
            Generate 3 search queries for a food safety database based on: "%s"
            1. Original query.
            2. Keyword-heavy query.
            3. $.
            Output ONLY lines separated by newlines.
            """;
        prompt = prompt.replace("$", technicalInstructions);
        try {
            String response = fastModel.getModel().chat(String.format(prompt, originalQuery));
            return Arrays.asList(response.split("\\n"));
        } catch (Exception e) {
            return List.of(originalQuery); // Fallback
        }
    }
}
// --- END OF FILE ---