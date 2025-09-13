package com.kairos.vector_search.service.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.InlineScript;
import org.opensearch.client.opensearch._types.ScriptLanguage;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.ScriptScoreQuery;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.ai_abstraction.service.EmbeddingModel;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.Searchable;
import com.kairos.search.annotation.SearchableField;
import com.kairos.search.model.SearchQuery;
import com.kairos.search.model.SearchResult;
import com.kairos.search.model.VectorSearcheable;
import com.kairos.search.query_builder.OpenSearchQueryBuilder;
import com.kairos.vector_search.model.VdbDocument;
import com.kairos.vector_search.service.VectorStoreService;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Json;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of VectorStoreService using ChromaDB via LangChain4j's
 * ChromaEmbeddingStore. This implementation is significantly simpler as it
 * delegates persistence logic to LangChain4j.
 */
@Service
@Slf4j
@AllArgsConstructor
public class OpenSearchStoreServiceImpl implements VectorStoreService {

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;
	private final OpenSearchClient osClient;
	private final OpenSearchQueryBuilder queryBuilder;
	private final ObjectMapper mapper;

	// We inject the generic EmbeddingStore interface, which will be the
	// ChromaEmbeddingStore bean we define.

	@Override
	public void addDocument(VdbDocument document) {
		log.info("Adding document with id: {} to Chroma.", document.getId());
		TextSegment segment = TextSegment.from(document.getContent(), toLangChainMetadata(document));
		Embedding embedding = embeddingModel.embed(segment).content();
		embeddingStore.add(embedding, segment);
	}

	@Override
	public void addDocuments(List<VdbDocument> documents) {
		log.info("Adding {} documents in batch to Chroma.", documents.size());
		List<TextSegment> segments = documents.stream()
				.map(doc -> TextSegment.from(doc.getContent(), toLangChainMetadata(doc))).collect(Collectors.toList());

		List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
		embeddingStore.addAll(embeddings, segments);
	}

	@Override
	public List<VdbDocument> findRelevant(String queryText, int topK) {
		log.debug("Finding top {} relevant documents for query: '{}'", topK, queryText);
		Embedding queryEmbedding = embeddingModel.embed(queryText).content();

		EmbeddingSearchResult<TextSegment> result = embeddingStore
				.search(EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).build());

		List<EmbeddingMatch<TextSegment>> matches = result.matches();

		return matches.stream()
				.map(match -> VdbDocument.builder().id(UUID.fromString(match.embeddingId()))
						.content(match.embedded().text()).metadata(match.embedded().metadata().toMap()).build())
				.collect(Collectors.toList());
	}

	/**
	 * Helper method to convert our internal VdbDocument metadata to LangChain4j's
	 * Metadata format.
	 */
	private Metadata toLangChainMetadata(VdbDocument document) {
		Metadata metadata = new Metadata();
		document.getMetadata().forEach((key, value) -> metadata.put(key, value.toString()));
		// Ensure our internal ID is preserved in the metadata for potential
		// cross-referencing
		metadata.put("internal_id", document.getId().toString());
		return metadata;
	}

	/**
	 * Dynamically builds the index mapping based on annotations in the entity
	 * class.
	 *
	 * @param entityClass The class of the entity to be indexed.
	 * @return A CreateIndexRequest.Builder with the mapping pre-configured.
	 */
	private CreateIndexRequest.Builder buildIndexMapping(Class<?> entityClass) {
		Searchable searchableAnnotation = entityClass.getAnnotation(Searchable.class);
		if (searchableAnnotation == null) {
			throw new IllegalArgumentException(
					"Entity class " + entityClass.getName() + " is not annotated with @Searchable");
		}

		Property textWithKeyword = new Property.Builder().text(new TextProperty.Builder()
				.fields("keyword",
						new Property.Builder().keyword(new KeywordProperty.Builder().ignoreAbove(256).build()).build())
				.build()).build();
		
		//Map<String,JsonData> hnswParameters = new HashMap<String, JsonData>();
		//hnswParameters.put("ef_construction", JsonData.of(128));
		//hnswParameters.put("m", JsonData.of(24));
		
		
		CreateIndexRequest.Builder createIndexRequestBuilder = new CreateIndexRequest.Builder()
				.settings(s -> s
	                    .knn(true) // This is the equivalent of "index.knn": true
	                   // .knnAlgoParamEfSearch(100) // Optional: tuning search performance
	                )
				.index(searchableAnnotation.indexName()).mappings(m -> {
					try {
						for (Field field : entityClass.getDeclaredFields()) {
							if (field.isAnnotationPresent(SearchableField.class)) {
								m.properties(field.getName(), textWithKeyword);
							} else if (field.isAnnotationPresent(GeoPointField.class)) {

								m.properties(field.getName(), p -> p.geoPoint(g -> g));
							}
						}
						m.properties("textEmbedding", p -> 
							p.knnVector(kv -> kv
									.dimension(embeddingModel.dimension())
									/*.method(method -> method
			                                .name("hnsw") // The algorithm used for indexing and search
			                                .spaceType("l2") // The distance metric (l2 is standard Euclidean distance)
			                                .engine("luceneknn") // The library that provides the HNSW implementation
			                                .parameters(hnswParameters) // Advanced tuning parameters
			                            )*/
								)
							);
						return m;
					} catch (Exception e) {
						throw new RuntimeException("Error building index mapping for " + entityClass.getName(), e);
					}
				});

		return createIndexRequestBuilder;
	}

	/**
	 * Creates the index in OpenSearch with the dynamically built mapping.
	 *
	 * @param entityClass The class of the entity to be indexed.
	 * @throws IOException If there is an error communicating with OpenSearch.
	 */
	public void createIndexWithMapping(Class<?> entityClass) throws IOException {
		CreateIndexRequest.Builder createIndexRequestBuilder = buildIndexMapping(entityClass);
		CreateIndexResponse createIndexResponse = osClient.indices().create(createIndexRequestBuilder.build());

		if (createIndexResponse.acknowledged()) {
			log.info("Index '{}' created successfully with mapping.", createIndexResponse.index());
		} else {
			log.error("Failed to create index '{}'.", createIndexResponse.index());
		}
	}

	@Override
	public void index(String indexName, UUID documentId, Object document) {
		try {
			log.info("Indexing document {} in index '{}'", documentId, indexName);

			if (document instanceof VectorSearcheable) {
				addEmbedding((VectorSearcheable) document);
			}

			osClient.index(i -> i.index(indexName).id(documentId.toString()).document(document));
		} catch (IOException e) {
			log.error("Failed to index document {} in OpenSearch", documentId, e);
			throw new RuntimeException("Failed to index document in OpenSearch", e);
		}
	}

	private void addEmbedding(VectorSearcheable vs) {
		String textContent = getTextContent(vs);
		if (textContent != null && !textContent.isBlank()) {
			float[] embedding = embeddingModel.embed(textContent).content().vector();
			vs.setTextEmbedding(embedding);
		}
	}

	private String getTextContent(Object document) {
		if (document.getClass().isAnnotationPresent(Searchable.class)) {
			try {
				String json = mapper.writeValueAsString(document);
				Map<String,Object> vals = mapper.readValue(json.getBytes(), new TypeReference<Map<String,Object>>() {
				});
				StringBuilder b = new StringBuilder();
				for (Field m : document.getClass().getDeclaredFields()) {
					if (m.isAnnotationPresent(SearchableField.class)) {

						String value = (String)vals.get(m.getName());
						b.append(value).append(" ");

					}
				}
				return b.toString().trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			

		} else {
			try {
				return mapper.writeValueAsString(document);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public SearchResult vectorSearch(SearchQuery query, Class<?> entityClass, int neighbours) {
		try {
			String indexName = entityClass.getAnnotation(Searchable.class).indexName();
			
			Query knnQuery = queryBuilder.buildVectorQuery(query, entityClass, embeddingModel);
			
		
			
			SearchResponse<Void> response = osClient.search(s -> s.index(indexName).query(knnQuery).size(neighbours) 
					.source(sf -> sf.fetch(false)), // Fetching only IDs
					Void.class);

			List<UUID> ids = response.hits().hits().stream().map(Hit::id).map(UUID::fromString)
					.collect(Collectors.toList());

			return new SearchResult(response.hits().total().value(), ids);
		} catch (Exception e) {
			throw new RuntimeException("Failed during OpenSearch vector search", e);
		}
	}

	// --- Unified Search Method ---
	@Override
	public SearchResult search(SearchQuery query, Class<?> entityClass) {
		String indexName = entityClass.getAnnotation(Searchable.class).indexName();
		log.debug("Executing unified search on index '{}' with query: {}", indexName, query);
		try {

			Query opensearchQuery = queryBuilder.buildQuery(query, entityClass);
			SearchResponse<Void> response = osClient.search(
					s -> s.index(indexName).query(opensearchQuery).size(100).source(sf -> sf.fetch(false)), Void.class);
			
			
			List<UUID> ids = response.hits().hits().stream().map(Hit::id).map(UUID::fromString)
					.collect(Collectors.toList());
			
			System.out.println(opensearchQuery.toJsonString());
			
			if(response.hits().total().value() == 0 || true) {
				return vectorSearch(query, entityClass, 10);
			}
			
			//

			return new SearchResult(response.hits().total().value(), ids);
		} catch (Exception e) {
			throw new RuntimeException("Failed during OpenSearch search", e);
		}
	}

	@Override
	public void delete(String indexName, UUID documentId) {
		try {
			log.info("Deleting document {} from index '{}'", documentId, indexName);

			osClient.delete(d -> d.index(indexName).id(documentId.toString()));
		} catch (IOException e) {
			log.error("Failed to delete document {} from OpenSearch", documentId, e);
			throw new RuntimeException("Failed to delete document from OpenSearch", e);
		}
	}

	@Override
	public void deleteAll(String indexName, List<UUID> documentIds) {
		try {
			log.info("Deleting document {} from index '{}'", documentIds, indexName);
			for (UUID documentId : documentIds) {
				osClient.delete(d -> d.index(indexName).id(documentId.toString()));
			}
		} catch (IOException e) {
			log.error("Failed to delete document {} from OpenSearch", documentIds, e);
			throw new RuntimeException("Failed to delete document from OpenSearch", e);
		}
	}
}