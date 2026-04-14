package com.kairos.autoconfigure;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.kairos.ai_abstraction.adapter.KairosEmbeddingModelAdaptor;
import com.kairos.autoconfigure.properties.KairosProperties;
import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.VectorStoreService;
import com.kairos.search.KairosVectorStoreService;
import com.kairos.search.retriever.InMemoryHybridRetriever;
import com.kairos.search.retriever.PostgresHybridRetriever;
import com.kairos.search.retriever.QdrantHybridRetriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

/**
 * Auto-configuration for Vector Stores with environment-aware defaults.
 * <p>
 * <b>Development:</b> Uses in-memory store (no PostgreSQL required).
 * <b>Production:</b> Auto-configures PgVector with connection pooling.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(EmbeddingStore.class)
@ConditionalOnProperty(prefix = "kairos.vector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KairosVectorStoreAutoConfiguration {
	
	
	 // =================================================================================
    // 1. IN-MEMORY (DEFAULT) - "Zero Config"
    // =================================================================================
    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "IN_MEMORY", matchIfMissing = true)
    public EmbeddingStore<TextSegment> inMemoryStore(KairosProperties props) {
        String filePath = props.getVector().getMemory().getPersistenceFile();
        log.info("KAIROS: Using In-Memory Vector Store (Persisted to: {})", filePath);
        
        try {
        // This enables persistence. It loads from file on startup, saves on shutdown.
        return  InMemoryEmbeddingStore.fromFile(filePath);
        }catch(Exception e) {
        	return new InMemoryEmbeddingStore<TextSegment>();
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "IN_MEMORY", matchIfMissing = true)
    public ContentRetriever inMemoryRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        return new InMemoryHybridRetriever(store, model, 20);
    }

    
 // =================================================================================
    // 2. QDRANT - "Lightweight Production"
    // =================================================================================
    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "QDRANT")
    public QdrantEmbeddingStore qdrantStore(KairosProperties props) {
        var qConfig = props.getVector().getQdrant();
        log.info("KAIROS: Connecting to Qdrant at {}:{}", qConfig.getHost(), qConfig.getPort());
        
        return QdrantEmbeddingStore.builder()
                .host(qConfig.getHost())
                .port(qConfig.getPort())
                .collectionName(qConfig.getCollectionName())
                .apiKey(qConfig.getApiKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "QDRANT")
    public ContentRetriever qdrantRetriever(QdrantEmbeddingStore store, EmbeddingModel model) {
        return new QdrantHybridRetriever(store, model);
    }
    
    // =================================================================================
    // 3. PGVECTOR - "Legacy / Heavy Production"
    // =================================================================================

    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "PGVECTOR")
    @ConditionalOnClass(dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore.class)
    public EmbeddingStore<TextSegment> pgVectorEmbeddingStore(KairosProperties properties, dev.langchain4j.model.embedding.EmbeddingModel model) {
        KairosProperties.Vector.PgVectorProperties props = properties.getVector().getPgvector();
        log.info("Configuring PgVector: {}@{}:{}/{}", 
        		props.getTableName(), props.getHost(), props.getPort(), props.getDatabase());
        
        // Extract credentials from DataSource if available, otherwise use properties
        return dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore.builder()
        		.host(props.getHost())
        		.port(props.getPort())
        		.createTable(true)
        		.database(props.getDatabase())
        		.dimension(model.dimension())
        		.password(props.getPassword())
        		.user(props.getUsername())
                .table(props.getTableName())
                .useIndex(true)
                .indexListSize(384)
                .metadataStorageConfig(new dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig() {
					
					@Override
					public dev.langchain4j.store.embedding.pgvector.MetadataStorageMode storageMode() {
						return dev.langchain4j.store.embedding.pgvector.MetadataStorageMode.COLUMN_PER_KEY;
					}
					
					@Override
					public List<String> indexes() {
						return Arrays.asList(
								"source_uri", 
								"source_filename", 
								"content_type", 
								"parent_content",
								"summary",
								"topics",
								"hypothetical_questions",
								"page_number",
								"section_type",
								"hierarchy_context"
								);
								
								 
					}
					 
					@Override
					public String indexType() {
						return "BTREE";
					}
					
					@Override
					public List<String> columnDefinitions() {
						return Arrays.asList(
								"source_uri text null", 
								"source_filename text null", 
								"content_type text null", 
								"processor text null",
								"has_visual_narrative text null",
								"parent_content text null",
								"summary text null",
								"topics text null",
								"hypothetical_questions text null",
								"section_type text null",
								"page_number text null",
								"hierarchy_context text null"
								);
					}
				})
                //.dropTableFirst(true)
                .build();
    }
    
    
    @Bean
    @ConditionalOnProperty(prefix = "kairos.vector", name = "provider", havingValue = "PGVECTOR")
    public ContentRetriever hybridRetriever(JdbcTemplate jdbc, EmbeddingModel model, KairosProperties props) {
        return new PostgresHybridRetriever(
            jdbc, 
            model, 
            props.getVector().getPgvector().getTableName(),
            20 // max results
        );
    }
    
    @Bean
    @ConditionalOnMissingBean(ContentRetriever.class)
    public ContentRetriever standardRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        // Uses LangChain4j's standard vector-only retrieval
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(model.getModel())
                .maxResults(20)
                .build();
    }
    
    @Bean
    public EmbeddingModel embeddingModel(dev.langchain4j.model.embedding.EmbeddingModel model) {
        return new KairosEmbeddingModelAdaptor(model);
    }
    
    @Bean
    @ConditionalOnMissingBean(dev.langchain4j.model.embedding.EmbeddingModel.class)
    public dev.langchain4j.model.embedding.EmbeddingModel defaultEmbeddingModel(){
    	return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    @Bean
    @ConditionalOnMissingBean(VectorStoreService.class)
    public VectorStoreService vectorStoreService(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
    	return new KairosVectorStoreService(store,model);
    }
    
}