package com.kairos.agentic.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.search.VdbDocument;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ResultRankingService {
	
    private  ChatLanguageModel reRankerModel;
	
	 /**
     * Industrial-grade re-ranking logic.
     * 1. Generates a strict context prompt.
     * 2. Uses Regex to extract numbers, ignoring surrounding text.
     * 3. Validates indices against the original list size.
     * 4. Deduplicates and preserves order.
     * 5. Falls back gracefully to original order on failure.
     */
    public List<VdbDocument> reRankResults(String query, List<VdbDocument> docs) {
        // Optimization: If we have very few results, don't waste time/money re-ranking.
        if (docs.size() <= 3) return docs;

        // 1. Build the prompt input with explicit indices
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            // Truncate content for the re-ranker to save tokens (Flash model has a large context, 
            // but we keep it efficient). 300 chars is usually enough to judge relevance.
            String snippet = docs.get(i).getContent().substring(0, Math.min(300, docs.get(i).getContent().length()));
            input.append("[").append(i).append("] ").append(snippet.replace("\n", " ")).append("\n");
        }

        String prompt = """
            You are a strict compliance relevance evaluator.
            Question: "%s"
            
            Candidates:
            %s
            
            Task: Select the top 5 most relevant candidates that answer the question.
            Output Format: Provide ONLY the numeric indices separated by commas (e.g., 0,4,2).
            If no candidates are relevant, return the top 1 based on keyword match.
            Do not write any explanation.
            """;

        try {
            // 2. Call Gemini Flash
            String response = reRankerModel.getModel().chat(String.format(prompt, query, input.toString()));
            log.debug("Re-ranker raw response: {}", response);

            // 3. Robust Parsing using Regex
            // This handles outputs like "0, 1, 2" or "[0], [1]" or "Indices: 0, 1"
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(response);

            List<VdbDocument> reorderedDocs = new ArrayList<>();
            Set<Integer> seenIndices = new LinkedHashSet<>(); // Use Set to prevent duplicates

            while (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group());
                    // 4. Bounds Checking & Deduplication
                    if (index >= 0 && index < docs.size() && !seenIndices.contains(index)) {
                        reorderedDocs.add(docs.get(index));
                        seenIndices.add(index);
                    }
                } catch (NumberFormatException e) {
                    // Ignore malformed numbers
                }
                // Cap at 5 results
                if (reorderedDocs.size() >= 5) break;
            }

            // 5. Validation
            if (reorderedDocs.isEmpty()) {
                log.warn("Re-ranker returned no valid indices. Falling back to original order.");
                return docs.subList(0, Math.min(5, docs.size()));
            }

            return reorderedDocs;

        } catch (Exception e) {
            log.error("Re-ranking failed due to error: {}. Returning original top 5.", e.getMessage());
            return docs.subList(0, Math.min(5, docs.size())); // Safe Fallback
        }
    }

}
