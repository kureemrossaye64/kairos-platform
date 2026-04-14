package com.kairos.search.postgres;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.GeoPointField;
import com.kairos.core.search.SearchQuery;
import com.kairos.core.search.SearchResult;
import com.kairos.core.search.Searchable;
import com.kairos.core.search.SearchableField;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorSearcheable;
import com.kairos.core.search.VectorStoreService;
import com.kairos.search.AbstractVectorStoreService;
import com.pgvector.PGvector;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class PgVectorStoreService extends AbstractVectorStoreService implements VectorStoreService {

    
    private final PgVectorQueryBuilder queryBuilder;
    
    
    
    public PgVectorStoreService(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel,
			EmbeddingStore<TextSegment> embeddingStore, ObjectMapper mapper, PgVectorQueryBuilder queryBuilder) {
		super(jdbcTemplate, embeddingModel, embeddingStore, mapper);
		this.queryBuilder = queryBuilder;
		// TODO Auto-generated constructor stub
	}

	public List<VdbDocument> findHybrid(String queryText, int topK, Class<?> entityClass) {
        SearchQuery query = SearchQuery.builder().textQuery(queryText).build();
        PgVectorQueryBuilder.PreparedQuery preparedQuery = queryBuilder.buildHybridSearchQuery(query, entityClass, embeddingModel, topK);

        // We query IDs first based on hybrid score
        List<Map<String,Object>> mids = jdbcTemplate.queryForList(
            preparedQuery.getSql(),
            //UUID.class,
            preparedQuery.getParams()
        );
        
        List<UUID> ids = mids.stream().map(m -> (UUID)m.get("id")).collect(Collectors.toList());

        if (ids.isEmpty()) return List.of();

        // Fetch actual content (preferring parent_content)
        String tableName = entityClass.getAnnotation(Searchable.class).indexName();
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        
        String fetchSql = String.format(
            "SELECT embedding_id, text, parent_content, source_filename, summary, topics, hypothetical_questions, section_type, hierarchy_context, page_number FROM %s WHERE embedding_id IN (%s)", 
            tableName, placeholders
        );

        return jdbcTemplate.query(fetchSql, ids.toArray(), (rs, rowNum) -> {
            String content = rs.getString("text");
            String parentContent = rs.getString("parent_content");
            String filename = rs.getString("source_filename");
            String textToUse = (parentContent != null && !parentContent.isBlank()) ? parentContent : content;
            
            String summary = rs.getString("summary");
            String topics = rs.getString("topics");
            String questions = rs.getString("hypothetical_questions");
            String sectionType = rs.getString("section_type");
            String hierarchy_context = rs.getString("hierarchy_context");
            
            Map<String,Object> metadata = new HashMap<String, Object>();
            if(summary != null) {
            	metadata.put("summary", summary);
            	
            }
            if(topics != null) {
            	metadata.put("topics", topics);
            }
            
            if(questions != null) {
            	metadata.put("hypothetical_questions", questions);
            }
            
            if(sectionType != null) {
            	metadata.put("section_type", sectionType);
            }
            
            if(filename != null) {
            	metadata.put("source_filename", filename);
            }
            
            if(hierarchy_context != null) {
            	metadata.put("hierarchy_context", hierarchy_context);
            }
            
            return VdbDocument.builder()
                    .id(UUID.fromString(rs.getString("embedding_id")))
                    .content(textToUse) 
                    .metadata(metadata)
                    .build();
        });
    }

    public List<VdbDocument> findRelevant(String queryText, int topK) {
        log.debug("Finding top {} relevant documents for query: '{}'", topK, queryText);
        Embedding queryEmbedding = embeddingModel.getModel().embed(queryText).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .filter(new IsEqualTo("entity", "vsearch"))
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> VdbDocument.builder()
                        .id(UUID.fromString(match.embeddingId()))
                        .content(match.embedded().text())
                        .metadata(match.embedded().metadata().toMap())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Direct JDBC Methods for Hybrid Search & Indexing ---

    public void index(UUID documentId, Object document) {
    	 Class<?> entityClass = document.getClass();
         Searchable searchable = entityClass.getAnnotation(Searchable.class);
         if (searchable == null) {
             log.warn("Attempted to index an object without @Searchable annotation: {}", entityClass.getSimpleName());
             return;
         }

         if (!(document instanceof VectorSearcheable)) {
              throw new IllegalArgumentException("Document to be indexed must implement VectorSearcheable.");
         }
         
         addEmbedding((VectorSearcheable)document);

         String tableName = searchable.indexName();
         String geoFieldName = findAnnotatedFieldName(entityClass, GeoPointField.class);

         try {
             // Use reflection to get values from the document instance
             UUID id = ((VectorSearcheable)document).getId();
             float[] embedding = ((VectorSearcheable) document).getTextEmbedding();
             Point coordinates = (Point) getFieldValue(document, geoFieldName);

             if (id == null || embedding == null || coordinates == null) {
                 log.error("Cannot index document with null id, embedding, or coordinates. ID: {}", id);
                 return;
             }

             String sql = String.format(
                 "INSERT INTO %s (id, text_embedding, %s) VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) " +
                 "ON CONFLICT (id) DO UPDATE SET " +
                 "text_embedding = EXCLUDED.text_embedding, " +
                 "%s = EXCLUDED.%s",
                 tableName, geoFieldName, geoFieldName, geoFieldName
             );

             jdbcTemplate.update(sql,
                 id,
                 new PGvector(embedding),
                 coordinates.getX(), // longitude
                 coordinates.getY()
             );

             log.info("Successfully indexed document with ID: {}", id);

         } catch (Exception e) {
             log.error("Failed to index document of type {}", entityClass.getSimpleName(), e);
             throw new RuntimeException("Indexing failed", e);
         }
    }

    /**
     * Creates a table in PostgreSQL with appropriate columns and indexes for searching.
     * This replaces the OpenSearch `createIndexWithMapping`.
     */
    public void createIndexWithMapping(Class<?> entityClass) {
    	Searchable searchable = entityClass.getAnnotation(Searchable.class);
        if (searchable == null) {
            throw new IllegalArgumentException("Entity class is not @Searchable: " + entityClass.getName());
        }
        String tableName = searchable.indexName();
        String geoFieldName = findAnnotatedFieldName(entityClass, GeoPointField.class);
        int dimension = embeddingModel.getModel().dimension();

        log.info("Generating DDL for search index table '{}' from entity {}", tableName, entityClass.getSimpleName());

        // 1. DDL for creating the table
        String createTableSql = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id UUID PRIMARY KEY, " +
            "text_embedding VECTOR(%d), " +
            "%s GEOGRAPHY(Point, 4326))",
            tableName, dimension, geoFieldName
        );

        // 2. DDL for the HNSW index for efficient vector search
        String createHnswIndexSql = String.format(
            "CREATE INDEX IF NOT EXISTS %s_embedding_hnsw_idx ON %s USING hnsw (text_embedding vector_l2_ops)",
            tableName, tableName
        );

        // 3. DDL for the GIST index for efficient geospatial search
        String createGistIndexSql = String.format(
            "CREATE INDEX IF NOT EXISTS %s_coords_gist_idx ON %s USING GIST (%s)",
            tableName, tableName, geoFieldName
        );

        log.info("Executing DDL for table '{}'", tableName);
        jdbcTemplate.execute(createTableSql);
        jdbcTemplate.execute(createHnswIndexSql);
        jdbcTemplate.execute(createGistIndexSql);
        log.info("Table and indexes for '{}' are ready.", tableName);
    }
    
    public SearchResult search(SearchQuery query, Class<?> entityClass) {
    	try {
            int resultLimit = 20; // Default limit
            PgVectorQueryBuilder.PreparedQuery preparedQuery = queryBuilder.buildSearchQuery(query, entityClass, embeddingModel, resultLimit);
            
            List<UUID> ids = jdbcTemplate.queryForList(
                preparedQuery.getSql(),
                UUID.class,
                preparedQuery.getParams()
            );
            
            return new SearchResult(ids.size(), ids);
        } catch (Exception e) {
            log.error("Failed during PostgreSQL hybrid search", e);
            throw new RuntimeException("Search failed", e);
        }
    }
    
    public SearchResult vectorSearch(SearchQuery query, Class<?> entityClass, int neighbours) {
        try {
            PgVectorQueryBuilder.PreparedQuery preparedQuery = queryBuilder.buildSearchQuery(query, entityClass, embeddingModel, neighbours);
            List<UUID> ids = jdbcTemplate.query(
                preparedQuery.getSql(),
                (rs, rowNum) -> UUID.fromString(rs.getString("id")),
                preparedQuery.getParams()
            );
            return new SearchResult(ids.size(), ids);
        } catch (Exception e) {
            throw new RuntimeException("Failed during PostgreSQL vector search", e);
        }
    }

    
}