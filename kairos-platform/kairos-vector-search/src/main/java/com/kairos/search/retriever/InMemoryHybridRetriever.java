package com.kairos.search.retriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import com.kairos.core.ai.EmbeddingModel;

@Slf4j
@RequiredArgsConstructor
public class InMemoryHybridRetriever implements ContentRetriever {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final int maxResults;

    @Override
    public List<Content> retrieve(Query query) {
        String question = query.text().toLowerCase();

        // 1. Vector Search (Semantic)
        var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.getModel().embed(query.text()).content())
                .maxResults(maxResults * 2) // Fetch more to filter later
                .build();
        
        var results = embeddingStore.search(searchRequest);

        // 2. "Hybrid" Reranking (Java Stream Logic)
        // We boost scores if the keyword actually appears in the text
        return results.matches().stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    double vectorScore = match.score();
                    double keywordScore = calculateKeywordScore(segment.text(), question);
                    
                    // Simple Weighted Formula: 70% Vector, 30% Keyword
                    double finalScore = (vectorScore * 0.7) + (keywordScore * 0.3);
                    
                    return new ScoredContent(Content.from(segment), finalScore);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Sort Descending
                .limit(maxResults)
                .map(ScoredContent::content)
                .collect(Collectors.toList());
    }

    private double calculateKeywordScore(String text, String query) {
        if (text == null) return 0.0;
        String lowerText = text.toLowerCase();
        // Naive containment check - 1.0 if contains, 0.0 if not.
        // Can be improved with Apache Lucene (Analysis) if needed later.
        return lowerText.contains(query) ? 1.0 : 0.0;
    }

    record ScoredContent(Content content, double score) {}
}