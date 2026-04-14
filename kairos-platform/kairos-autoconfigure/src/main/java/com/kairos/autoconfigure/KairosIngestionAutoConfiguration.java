package com.kairos.autoconfigure;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kairos.core.ai.AudioAnalysisService;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ai.ImageAnalysisService;
import com.kairos.core.ai.TextAnalysisService;
import com.kairos.core.ai.VideoAnalysisService;
import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.core.ingestion.SourceRecordService;
import com.kairos.core.search.VectorStoreService;
import com.kairos.core.storage.StorageService;
import com.kairos.ingestion.configs.FileIngestionProperties;
import com.kairos.ingestion.graph.impl.JGraphTRepository;
import com.kairos.ingestion.graph.spi.KnowledgeGraphRepository;
import com.kairos.ingestion.impl.GeminiVisionParser;
import com.kairos.ingestion.persistence.InMemorySourceRecordService;
import com.kairos.ingestion.persistence.JdbcSourceRecordService;
import com.kairos.ingestion.processor.AiEnrichmentProcessor;
import com.kairos.ingestion.processor.AudioTranscriptionProcessor;
import com.kairos.ingestion.processor.GraphIngestionProcessor;
import com.kairos.ingestion.processor.HierarchicalProcessor;
import com.kairos.ingestion.processor.ImageAnalysisProcessor;
import com.kairos.ingestion.processor.ParentChildSplitterProcessor;
import com.kairos.ingestion.processor.TextCleaningProcessor;
import com.kairos.ingestion.processor.TikaDocumentParserProcessor;
import com.kairos.ingestion.processor.VideoAnalysisProcessor;
import com.kairos.ingestion.rag.IngestionRouter;
import com.kairos.ingestion.service.LibrarianAgent;
import com.kairos.ingestion.source.FolderWatcherService;
import com.kairos.ingestion.source.SourcePersistenceService;
import com.kairos.ingestion.spi.DocumentParser;
import com.kairos.ingestion.utils.TextSlicer;

/**
 * Auto-configuration for Document Ingestion Pipeline.
 * <p>
 * Provides file system watching and ingestion routing.
 */
@AutoConfiguration
@ConditionalOnClass(IIngestionRouter.class)
public class KairosIngestionAutoConfiguration {
	
	@Bean
    @ConditionalOnMissingBean
	public ObjectMapper objectMapper() {
		ObjectMapper m = new ObjectMapper();
		m.registerModule(new Jdk8Module());
		m.registerModule(new JavaTimeModule());
		m.registerModule(new JtsModule());
		return m;
	}

	@Bean
	@ConditionalOnProperty(prefix = "kairos.persistence", name = "provider", havingValue = "IN_MEMORY", matchIfMissing = true)
	@ConditionalOnMissingBean(SourceRecordService.class)
	public SourceRecordService inMemorySourceRecordService() {
		return new InMemorySourceRecordService();
	}

	@Bean
	@ConditionalOnProperty(prefix = "kairos.persistence", name = "provider", havingValue = "JDBC")
	@ConditionalOnMissingBean(SourceRecordService.class)
	public SourceRecordService jdbcSourceRecordService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		return new JdbcSourceRecordService(jdbcTemplate, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public SourcePersistenceService sourcePersistenceService(StorageService storageService,
			SourceRecordService sourceRecordService, ApplicationEventPublisher eventPublisher) {
		return new SourcePersistenceService(storageService, sourceRecordService, eventPublisher);
	}

	@Bean
	@ConditionalOnMissingBean
	public TextSlicer textSlicer() {
		return new TextSlicer();
	}

	// --- 2. Processors (The Worker Bees) ---
	// We define each processor as a bean so they can be overridden individually if
	// needed.

	@Bean
	@ConditionalOnMissingBean
	public TextCleaningProcessor textCleaningProcessor() {
		return new TextCleaningProcessor();
	}

	@Bean
	@ConditionalOnMissingBean
	public AiEnrichmentProcessor aiEnrichmentProcessor(TextAnalysisService textAnalysisService) {
		return new AiEnrichmentProcessor(textAnalysisService);
	}

	@Bean
	@ConditionalOnMissingBean
	public AudioTranscriptionProcessor audioTranscriptionProcessor(AudioAnalysisService audioService) {
		return new AudioTranscriptionProcessor(audioService);
	}

	@Bean
	@ConditionalOnMissingBean
	public VideoAnalysisProcessor videoAnalysisProcessor(VideoAnalysisService videoService) {
		return new VideoAnalysisProcessor(videoService);
	}

	@Bean
	@ConditionalOnMissingBean
	public ImageAnalysisProcessor imageAnalysisProcessor(ImageAnalysisService imageService) {
		return new ImageAnalysisProcessor(imageService);
	}

	@Bean
	@ConditionalOnMissingBean
	public TikaDocumentParserProcessor tikaDocumentParserProcessor(StorageService storageService) {
		return new TikaDocumentParserProcessor(storageService);
	}

	@Bean
	@ConditionalOnMissingBean
	public ParentChildSplitterProcessor parentChildSplitterProcessor() {
		return new ParentChildSplitterProcessor();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public DocumentParser documentParser(@Qualifier("pro") ChatLanguageModel chatModel, ObjectMapper objectMapper, TextSlicer slicer) {
		//return new AdvancedLayoutAnalysisProcessor(storageService, chatModel, objectMapper);
		
		return new GeminiVisionParser(chatModel,objectMapper,slicer);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public KnowledgeGraphRepository knowledgeRepository(ObjectMapper objectMapper, @Value("${app.graph.storage-path:./data/graph-snapshot.json}")String storagePath) {
		return new JGraphTRepository(objectMapper, storagePath);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public LibrarianAgent librarianAgent(KnowledgeGraphRepository graphRepository, @Qualifier("pro") ChatLanguageModel chatModel, ObjectMapper objectMapper) {
		return new LibrarianAgent(graphRepository, chatModel, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public GraphIngestionProcessor graphIngestionProcessor(StorageService storageService,DocumentParser documentParser, LibrarianAgent librarianAgent) {
		//return new AdvancedLayoutAnalysisProcessor(storageService, chatModel, objectMapper);
		
		return new GraphIngestionProcessor(storageService, documentParser, librarianAgent);
	}

	@Bean
	@ConditionalOnMissingBean
	public HierarchicalProcessor hierarchicalProcessor(StorageService storageService,
			@Qualifier("pro") ChatLanguageModel chatModel, ObjectMapper objectMapper, TextSlicer textSlicer) {
		return new HierarchicalProcessor(storageService, chatModel, objectMapper, textSlicer);
	}

	// --- 3. The Router (The Orchestrator) ---

	@Bean
	@ConditionalOnMissingBean(IIngestionRouter.class)
	public IngestionRouter ingestionRouter(SourceRecordService sourceRecordService, VectorStoreService searchService,
			AudioTranscriptionProcessor audioProcessor, VideoAnalysisProcessor videoProcessor,
			ImageAnalysisProcessor imageProcessor, TikaDocumentParserProcessor tikaProcessor,
			GraphIngestionProcessor graphIngestionProcessor, ParentChildSplitterProcessor splitterProcessor,
			HierarchicalProcessor hierarchicalProcessor, TextCleaningProcessor cleaningProcessor,
			AiEnrichmentProcessor enrichmentProcessor, SourcePersistenceService persistenceService) {
		return new IngestionRouter(sourceRecordService, searchService, audioProcessor, videoProcessor, imageProcessor,
				tikaProcessor, graphIngestionProcessor, splitterProcessor, hierarchicalProcessor, cleaningProcessor,
				enrichmentProcessor, persistenceService);
	}

	@Configuration
	@ConditionalOnProperty(prefix = "kairos.ingestion.watcher", name = "enabled", havingValue = "true")
	@EnableScheduling
	static class FileWatcherConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public FolderWatcherService folderWatcherService(FileIngestionProperties properties, IIngestionRouter router) {
			return new FolderWatcherService(properties, router);
		}

		@Bean
		@ConditionalOnMissingBean
		public FileIngestionProperties fileIngestionProperties() {
			return new FileIngestionProperties();
		}
	}
}