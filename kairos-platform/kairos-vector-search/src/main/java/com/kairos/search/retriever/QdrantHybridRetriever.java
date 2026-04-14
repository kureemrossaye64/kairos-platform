package com.kairos.search.retriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.kairos.core.ai.EmbeddingModel;

@RequiredArgsConstructor
public class QdrantHybridRetriever implements ContentRetriever {

    private final QdrantEmbeddingStore qdrantStore;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<Content> retrieve(Query query) {
        // Qdrant (via LangChain4j) supports metadata filtering.
        // While full "Hybrid" (Sparse/Dense) support is coming to LangChain4j,
        // for now we rely on strong Vector Search.
        // Note: You can add Filter objects here if you want to restrict by "source_filename" etc.
        
        return qdrantStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.getModel().embed(query.text()).content())
                .maxResults(20)
                .build())
                .matches().stream()
                .map(match -> Content.from(TextSegment.from(  match.embedded().text(), match.embedded().metadata())))
                .collect(Collectors.toList());
    }
}