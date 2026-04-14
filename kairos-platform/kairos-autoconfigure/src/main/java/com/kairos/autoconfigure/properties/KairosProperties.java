package com.kairos.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Root configuration properties for KAIROS Platform.
 * <p>
 * All features are enabled by default with sensible development defaults.
 * Production tuning should override these via application.yml or environment
 * variables.
 */
@ConfigurationProperties(prefix = "kairos")
@Validated
@Data
public class KairosProperties {

	/**
	 * Application name used in logs and default user-agent strings.
	 */
	private String applicationName = "kairos-app";

	/**
	 * Enable/disable the entire KAIROS platform.
	 */
	private boolean enabled = true;

	/**
	 * Development mode - when true, uses in-memory or simplified implementations to
	 * reduce external dependencies during development.
	 */
	private boolean devMode = true;

	private final Vector vector = new Vector();
	private final Crawler crawler = new Crawler();
	
	
	private final Persistence persistence = new Persistence();
	
	private final Storage storage = new Storage();

    @Data
    public static class Storage {
        private StorageProvider provider = StorageProvider.LOCAL; // Default
        
        private LocalProperties local = new LocalProperties();
        private GcsProperties gcs = new GcsProperties();

        public enum StorageProvider { LOCAL, GCS }
    }

    @Data
    public static class LocalProperties {
        private String rootDirectory = "./data/storage";
    }

    @Data
    public static class GcsProperties {
        private String bucketName;
        private String projectId;
    }

    @Data
    public static class Persistence {
        /**
         * Storage mechanism for internal KAIROS metadata (SourceRecords, CrawlJobs).
         * Options: IN_MEMORY, JDBC
         */
        private StorageProvider provider = StorageProvider.IN_MEMORY;
        
        public enum StorageProvider { IN_MEMORY, JDBC }
    }

	

	@Data
	public static class Vector {
		
		private VectorStoreProvider provider = VectorStoreProvider.IN_MEMORY; 
	    
	    private PgVectorProperties pgvector = new PgVectorProperties();
	    
	    private QdrantProperties qdrant = new QdrantProperties();
	    
	    private InMemoryProperties memory = new InMemoryProperties();
		
		
		@Data
		public static class PgVectorProperties {
			
			private String host = "localhost";
			
			private Integer port = 5432;
			
			private String username = "postgres";
			
			private String password = "postgres";
			
			private String tableName = "embeddings";

			private Integer dimension = 384;

			private String database = "kairos";
		}
		
		@Data
	    public static class QdrantProperties {
	        private String host = "localhost";
	        
	        private int port = 6334;
	        
	        private String collectionName = "kairos_knowledge";
	        
	        private String apiKey = null;
	    }
		
		@Data
		public static class InMemoryProperties{
			private String persistenceFile = "./data/embeddings.json";
		}
		
		public enum VectorStoreProvider { PGVECTOR, QDRANT, IN_MEMORY }

	}

	@Data
	public static class Crawler {
		/**
		 * Enable distributed crawling.
		 */
		private boolean enabled = false;

		/**
		 * User agent string.
		 */
		private String userAgent = "KairosBot/1.0 (+https://example.com/bot)";

		/**
		 * Politeness delay in milliseconds.
		 */
		private int politenessDelay = 2000;

		/**
		 * Maximum crawl depth.
		 */
		private int maxDepth = 5;

		/**
		 * Use RabbitMQ for distributed crawling (false = in-memory queue).
		 */
		private boolean distributed = false;
		
		private int maxPagesToFetch = -1;

	}

}