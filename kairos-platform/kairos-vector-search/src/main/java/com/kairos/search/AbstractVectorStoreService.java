package com.kairos.search;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.Searchable;
import com.kairos.core.search.SearchableField;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorSearcheable;
import com.kairos.core.search.VectorStoreService;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public abstract class AbstractVectorStoreService implements VectorStoreService{
	
	protected final JdbcTemplate jdbcTemplate;
	
	protected final EmbeddingModel embeddingModel;
	
	protected final EmbeddingStore<TextSegment> embeddingStore;
	
	protected final ObjectMapper mapper;
	
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
	
    public void delete(Class<?> entityClass, UUID documentId) {
    	String indexName = getTableName(entityClass);
    	log.info("Deleting document {} from table '{}'", documentId, indexName);
        String sql = String.format("DELETE FROM %s WHERE id = ?", indexName);
        jdbcTemplate.update(sql, documentId);
    }

    public void deleteAll(Class<?> entityClass, List<UUID> documentIds) {
    	String indexName = getTableName(entityClass);
        log.info("Deleting {} documents from table '{}'", documentIds.size(), indexName);
        String idsPlaceholder = documentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("DELETE FROM %s WHERE id IN (%s)", indexName, idsPlaceholder);
        jdbcTemplate.update(sql, documentIds.toArray());
    }

    // --- Helper Methods ---

    protected void addEmbedding(VectorSearcheable vs) {
        String textContent = getTextContent(vs);
        if (textContent != null && !textContent.isBlank()) {
            float[] embedding = embeddingModel.getModel().embed(textContent).content().vector();
            vs.setTextEmbedding(embedding);
        }
    }

    protected String getTextContent(Object document) {
        if (!document.getClass().isAnnotationPresent(Searchable.class)) {
            try {
                return mapper.writeValueAsString(document);
            } catch (JsonProcessingException e) {
                log.error("Error serializing non-searchable object to string for embedding", e);
                return "";
            }
        }

        try {
            Map<String, Object> vals = mapper.convertValue(document, new TypeReference<>() {});
            StringBuilder b = new StringBuilder();
            for (Field field : document.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(SearchableField.class)) {
                    Object value = vals.get(field.getName());
                    if (value != null) {
                        b.append(value).append(" ");
                    }
                }
            }
            return b.toString().trim();
        } catch (Exception e) {
            log.error("Error reflecting fields to build text content for embedding", e);
            return "";
        }
    }
    
    protected Metadata toLangChainMetadata(VdbDocument document) {
        Metadata metadata = new Metadata();
        document.getMetadata().forEach((key, value) -> metadata.put(key, value.toString()));
        metadata.put("internal_id", document.getId().toString());
        metadata.put("entity", "vsearch");
        return metadata;
    }

    @SuppressWarnings("unchecked")
    protected String findAnnotatedFieldName(Class<?> clazz, @SuppressWarnings("rawtypes") Class annotation) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing @" + annotation.getSimpleName() + " annotation on class " + clazz.getName()));
    }

    protected Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    protected String getTableName(Class<?> entityClass) {
        Searchable searchableAnnotation = entityClass.getAnnotation(Searchable.class);
        if (searchableAnnotation == null || searchableAnnotation.indexName().isBlank()) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Searchable or indexName is empty.");
        }
        return searchableAnnotation.indexName();
    }

}
