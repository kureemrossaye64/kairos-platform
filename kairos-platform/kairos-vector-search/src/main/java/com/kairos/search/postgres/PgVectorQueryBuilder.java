package com.kairos.search.postgres;

import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.GeoPointField;
import com.kairos.core.search.SearchQuery;
import com.kairos.core.search.Searchable;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PgVectorQueryBuilder {

    private final Map<Class<?>, String> geoFieldCache = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    public static class PreparedQuery {
        private final String sql;
        private final Object[] params;
    }
    
    
    public PreparedQuery buildHybridSearchQuery(SearchQuery searchQuery, Class<?> entityClass, EmbeddingModel embeddingModel, int limit) {
    	String tableName = getTableName(entityClass);
        List<Object> params = new ArrayList<>();

        // We use a Common Table Expression (CTE) to perform Hybrid Ranking
        // Vector Weight: 0.7, Keyword Weight: 0.3
        StringBuilder sql = new StringBuilder();
        
        // 1. Generate Embedding
        float[] queryVector = embeddingModel.getModel().embed(searchQuery.getTextQuery()).content().vector();

        // FIX: Used 'embedding_id' instead of 'id'
        // FIX: Used 'embedding' instead of 'text_embedding'
        // FIX: Added '::vector' cast to the parameter
        sql.append("WITH vector_search AS ( ")
           .append("  SELECT embedding_id, 1 - (embedding <=> ?::vector) AS vector_score ") // Cosine similarity approx
           .append("  FROM ").append(tableName)
           .append("  ORDER BY vector_score DESC LIMIT ? ")
           .append("), ")
           .append("keyword_search AS ( ")
           .append("  SELECT embedding_id, ts_rank_cd(content_tsvector, plainto_tsquery('english', ?)) AS keyword_score ")
           .append("  FROM ").append(tableName)
           .append("  WHERE content_tsvector @@ plainto_tsquery('english', ?) ")
           .append("  LIMIT ? ")
           .append(") ")
           .append("SELECT ")
           // We alias the final ID as 'id' so the RowMapper works easily
           .append("  COALESCE(v.embedding_id, k.embedding_id) as id, ")
           .append("  COALESCE(v.vector_score, 0) * 0.7 + COALESCE(k.keyword_score, 0) * 0.3 as final_score ")
           .append("FROM vector_search v ")
           .append("FULL OUTER JOIN keyword_search k ON v.embedding_id = k.embedding_id ")
           .append("ORDER BY final_score DESC ")
           .append("LIMIT ?");

        // Params mapping
        params.add(new PGvector(queryVector)); // Vector param
        params.add(limit * 3);                 // Vector limit (fetch more candidates)
        params.add(searchQuery.getTextQuery()); // Keyword param 1
        params.add(searchQuery.getTextQuery()); // Keyword param 2
        params.add(limit * 3);                 // Keyword limit
        params.add(limit);                     // Final limit

        return new PreparedQuery(sql.toString(), params.toArray());
    }

    /**
     * Builds a hybrid vector and geospatial search query.
     *
     * @return A PreparedQuery containing the SQL and its parameters.
     */
    public PreparedQuery buildSearchQuery(SearchQuery searchQuery, Class<?> entityClass, EmbeddingModel embeddingModel, int limit) {
        if (searchQuery.getTextQuery() == null && searchQuery.getLocation() == null) {
            throw new IllegalArgumentException("Search query must contain at least a text query or a location.");
        }

        String tableName = getTableName(entityClass);
        Optional<String> geoFieldNameOpt = getGeoField(entityClass);

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id FROM ").append(tableName);
        StringBuilder whereClause = new StringBuilder();

        // 1. Build Geospatial Filter (WHERE clause)
        if (searchQuery.getLocation() != null && searchQuery.getRadiusInKm() != null) {
            String geoFieldName = geoFieldNameOpt.orElseThrow(() ->
                    new IllegalArgumentException("Geospatial search requires a @GeoPointField on " + entityClass.getName()));

            // ST_DWithin is efficient with a GIST index. We use 'geography' type for meter-based distance.
            whereClause.append("ST_DWithin(").append(geoFieldName).append(", ST_MakePoint(?, ?)::geography, ?)");
            params.add(searchQuery.getLocation().getX()); // Longitude
            params.add(searchQuery.getLocation().getY()); // Latitude
            params.add(searchQuery.getRadiusInKm() * 1000); // Convert km to meters
        }

        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        // 2. Build Vector Similarity Ranking (ORDER BY clause)
        if (StringUtils.hasText(searchQuery.getTextQuery())) {
            // Embed the query text to get the search vector
            float[] queryVector = embeddingModel.getModel().embed(searchQuery.getTextQuery()).content().vector();
            // The <-> operator from pgvector calculates L2 distance.
            sql.append(" ORDER BY text_embedding <-> ?");
            params.add(new PGvector(queryVector));
        }

        sql.append(" LIMIT ?");
        params.add(limit);

        log.debug("Generated SQL: {}", sql);
        return new PreparedQuery(sql.toString(), params.toArray());
    }

    private String getTableName(Class<?> entityClass) {
        Searchable searchableAnnotation = entityClass.getAnnotation(Searchable.class);
        if (searchableAnnotation == null || searchableAnnotation.indexName().isBlank()) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Searchable or indexName is empty.");
        }
        return searchableAnnotation.indexName();
    }

    private Optional<String> getGeoField(Class<?> entityClass) {
        return Optional.ofNullable(geoFieldCache.computeIfAbsent(entityClass, clazz -> {
            log.debug("Reflecting @GeoPointField annotation for class: {}", clazz.getSimpleName());
            return Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(GeoPointField.class))
                    .map(Field::getName)
                    .findFirst()
                    .orElse(null);
        }));
    }
}