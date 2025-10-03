package com.kairos.sports_atlas.config;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kairos.core.ai.EmbeddingModel;
import com.kairos.core.search.VectorStoreService;
import com.kairos.search.postgres.PgVectorProperties;
import com.kairos.search.postgres.PgVectorQueryBuilder;
import com.kairos.search.postgres.PgVectorStoreService;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

/**
 * Configures the connection to the ChromaDB vector store. This configuration is
 * activated only when "kairos.vector-store.chroma.base-url" is set.
 */
@Configuration
@ConditionalOnProperty(name = "kairos.vector-store.pgvector.host")
@Slf4j
public class VectorStoreConfig {
	
	@Value("${kairos.vector-store.pgvector.host:localhost}")
	private String host;
	
	@Value("${kairos.vector-store.pgvector.port:5432}")
	private Integer port;
	
	@Value("${kairos.vector-store.pgvector.table-name:embeddings}")
	private String tableName;
	
	@Value("${kairos.vector-store.pgvector.username:postgres}")
	private String username;
	
	@Value("${kairos.vector-store.pgvector.password:postgres}")
	private String password;
	
	@Value("${kairos.vector-store.pgvector.database:sports}")
	private String database;
	
	@Value("${kairos.vector-store.pgvector.timeout-seconds:60}")
	private Integer timeoutSeconds;
	
	@Value("${kairos.vector-store.pgvector.dimension:384}")
	private Integer dimension;
	
	@Bean
	public PgVectorProperties pgVectorProperties() {
		return PgVectorProperties.builder().database(database).dimension(dimension).host(host)
				.password(password).port(port).tableName(tableName).username(username).build();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper m = new ObjectMapper();
		m.registerModule(new Jdk8Module());
		m.registerModule(new JavaTimeModule());
		m.registerModule(new JtsModule());
		return m;
	}
	
	 

		@Bean
		public EmbeddingStore<TextSegment> pgVectorEmbeddingStore(PgVectorProperties props) {
	        log.info("Creating PgVectorEmbeddingStore for table '{}' with dimension {}", props.getTableName(), props.getDimension());
	        return PgVectorEmbeddingStore.builder()
	        		.host(props.getHost())
	        		.port(props.getPort())
	        		.createTable(true)
	        		.database(props.getDatabase())
	        		.dimension(props.getDimension())
	        		.password(props.getPassword())
	        		.user(props.getUsername())
	                .table(props.getTableName())
	                .dimension(props.getDimension())
	                .useIndex(true)
	                .indexListSize(384)
	                //.dropTableFirst(true)
	                .build();
		}
		
		
		
		@Bean
		public VectorStoreService vectorStoreService(
				EmbeddingStore<TextSegment> embeddingStore,
			    EmbeddingModel embeddingModel,
			    JdbcTemplate jdbcTemplate,
			    PgVectorQueryBuilder queryBuilder,
			    ObjectMapper mapper
				) {
			return new PgVectorStoreService(embeddingStore,embeddingModel,jdbcTemplate,queryBuilder,mapper);
		}

}