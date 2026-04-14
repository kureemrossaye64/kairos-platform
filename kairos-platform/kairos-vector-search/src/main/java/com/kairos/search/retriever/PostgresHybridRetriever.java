package com.kairos.search.retriever;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.kairos.core.ai.EmbeddingModel;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostgresHybridRetriever implements ContentRetriever {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final String tableName;
    private final int maxResults;

    @Override
    public List<Content> retrieve(Query query) {
        String question = query.text();
        float[] vector = embeddingModel.getModel().embed(question).content().vector();

        // Your specific Hybrid SQL (CTE)
        String sql = """
           WITH vector_search AS (
               SELECT embedding_id, 1 - (embedding <=> ?::vector) AS vector_score
               FROM %s
               ORDER BY vector_score DESC LIMIT ?
           ),
           keyword_search AS (
               SELECT embedding_id, ts_rank_cd(to_tsvector('english', text), plainto_tsquery('english', ?)) AS keyword_score
               FROM %s
               WHERE to_tsvector('english', text) @@ plainto_tsquery('english', ?)
               LIMIT ?
           )
           SELECT
               COALESCE(v.embedding_id, k.embedding_id) as id,
               t.text,
               t.parent_content, -- Fetch parent content for better context
               t.source_filename,
               COALESCE(v.vector_score, 0) * 0.7 + COALESCE(k.keyword_score, 0) * 0.3 as final_score
           FROM vector_search v
           FULL OUTER JOIN keyword_search k ON v.embedding_id = k.embedding_id
           JOIN %s t ON t.embedding_id = COALESCE(v.embedding_id, k.embedding_id)
           ORDER BY final_score DESC
           LIMIT ?
        """.formatted(tableName, tableName, tableName);
        
        

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String text = rs.getString("text");
            String parent = rs.getString("parent_content");
            String filename = rs.getString("source_filename");
            
            // Logic: Use parent content if available (Small-to-Big RAG), else chunk text
            String contentText = (parent != null && !parent.isBlank()) ? parent : text;
            
            return Content.from(TextSegment.from(contentText, Metadata.from("source", filename)));
        }, 
        // Parameters for PreparedStatement
        new Object[]{ 
            new com.pgvector.PGvector(vector), // Vector
            maxResults * 2,                    // Vector Limit
            question,                          // Keyword Query 1
            question,                          // Keyword Query 2
            maxResults * 2,                    // Keyword Limit
            maxResults                         // Final Limit
        });
    }
}