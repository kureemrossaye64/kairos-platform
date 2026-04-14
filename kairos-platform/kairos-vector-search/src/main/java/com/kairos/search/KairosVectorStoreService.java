package com.kairos.search;

import java.util.List;
import java.util.stream.Collectors;

import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorStoreService;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class KairosVectorStoreService implements VectorStoreService{
	
	private final EmbeddingStore<TextSegment> embeddingStore;
	
	private final EmbeddingModel embeddingModel;

	@Override
    public void addDocument(VdbDocument document) {
        log.info("Adding document with id: {} to pgvector.", document.getId());
        TextSegment segment = TextSegment.from(document.getContent(), toLangChainMetadata(document));
        Embedding embedding = embeddingModel.getModel().embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    @Override
    public void addDocuments(List<VdbDocument> documents) {
        log.info("Adding {} documents in batch to pgvector.", documents.size());
        List<TextSegment> segments = documents.stream()
                .map(doc -> TextSegment.from(doc.getContent(), toLangChainMetadata(doc)))
                .collect(Collectors.toList());

        List<Embedding> embeddings = embeddingModel.getModel().embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
    }
    
    private Metadata toLangChainMetadata(VdbDocument document) {
        Metadata metadata = new Metadata();
        document.getMetadata().forEach((key, value) -> metadata.put(key, value.toString()));
        metadata.put("internal_id", document.getId().toString());
        metadata.put("entity", "vsearch");
        return metadata;
    }

}
