package com.kairos.search.query_builder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.InlineScript;
import org.opensearch.client.opensearch._types.ScriptLanguage;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.GeoDistanceQuery;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.ScriptScoreQuery;
import org.springframework.stereotype.Component;

import com.kairos.ai_abstraction.service.EmbeddingModel;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.Searchable;
import com.kairos.search.annotation.SearchableField;
import com.kairos.search.model.SearchQuery;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenSearchQueryBuilder {

	// A cache to store the reflected metadata of our entities for performance.
	private final Map<Class<?>, List<String>> searchableFieldsCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> geoFieldCache = new ConcurrentHashMap<>();

	public Query buildQuery(SearchQuery searchQuery, Class<?> entityClass) {
		if (!entityClass.isAnnotationPresent(Searchable.class)) {
			throw new IllegalArgumentException(
					"Class " + entityClass.getName() + " is not annotated with @Searchable.");
		}

		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		// 1. Build the Text Search component (if applicable)
		if (searchQuery.textQuery() != null && !searchQuery.textQuery().isBlank()) {
			List<String> fieldsToSearch = getSearchableFields(entityClass);
			if (!fieldsToSearch.isEmpty()) {
				boolQuery.must(m -> m
						.multiMatch(mm -> mm.query(searchQuery.textQuery()).fields(fieldsToSearch).fuzziness("AUTO")));
			}
		}

		// 2. Build the Geospatial component (if applicable)
		if (searchQuery.location() != null && searchQuery.radiusInKm() != null) {
			getGeoField(entityClass).ifPresent(geoFieldName -> {
				boolQuery.filter(f -> f.geoDistance(gd -> gd.field(geoFieldName)
						.location(loc -> loc
								.latlon(ll -> ll.lat(searchQuery.location().getY()).lon(searchQuery.location().getX())))
						.distance(searchQuery.radiusInKm() + "km")));
			});
		}

		// ... (Future: Add logic for keywordFilters here) ...

		return new Query.Builder().bool(boolQuery.build()).build();
	}
	
	
	public Query buildVectorQuery(SearchQuery searchQuery, Class<?> entityClass, EmbeddingModel embeddingModel) {
		String queryText = searchQuery.textQuery();
		// 1. Convert the query text into a vector
		float[] queryVector = embeddingModel.embed(queryText).content().vector();
		List<Float> lvectors = embeddingModel.embed(queryText).content().vectorAsList();
		KnnQuery kkk = KnnQuery.of(kq -> kq.field("textEmbedding").vector(lvectors).k(100));
		
		
		ScriptScoreQuery scriptScoreQuery = ScriptScoreQuery.of(q -> q.minScore(0f)
                .query(Query.of(qu -> qu.matchAll(m -> m)))
                .script(s -> s.inline(InlineScript.of(i -> i
                        .source("knn_score")
                        .lang(ScriptLanguage.builder().custom("knn").build())
                        .params("field", JsonData.of("textEmbedding"))
                        .params("query_value", JsonData.of(queryVector))
                        .params("space_type", JsonData.of("cosinesimil")))))
                .boost(0.5f));
		
		
		if (searchQuery.location() != null && searchQuery.radiusInKm() != null ) {
			
			Optional<String> geo = getGeoField(entityClass);
			if(geo.isPresent()) {
				/*GeoDistanceQuery geoDistanceQuery = GeoDistanceQuery.of(q -> q
				        .field(geo.get()) // The name of your geo_point field
				        .distance(searchQuery.radiusInKm() + "km") // e.g., "10km", "5mi"
				        .location(loc -> loc
				            .latlon(ll -> ll.lat(searchQuery.location().getX()).lon(searchQuery.location().getY()))
				        )
				    );
			
				KnnQuery kk = KnnQuery.of(kq -> kq
						.field("textEmbedding")
						.vector(lvectors)
						.k(100)
						.filter(f -> f.geoDistance(geoDistanceQuery)));
				
				Query knnQuery = Query.of(n -> n.knn(kk));
				
				System.out.println(knnQuery.toJsonString());
				
			
				
				return knnQuery;*/
				
				String fname = geo.get();
				var lat = searchQuery.location().getX();
				var lon = searchQuery.location().getY();
				Query knnQuery = Query.of(q -> q
					    .knn(k -> k
					        .field("textEmbedding")
					        .vector(lvectors)
					        .k(100)
					        // The filter is applied BEFORE the k-NN search runs
					        /*.filter(f -> f
					            .geoDistance(gd -> gd
					                .field(fname)
					                .distance("10km")
					                .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
					            )
					        )*/
					    )
					);
				System.out.println(knnQuery.toJsonString());
				return knnQuery;
				
			}
		}
		
		
		//Query knnQuery = Query.of(n -> n.scriptScore(scriptScoreQuery));
		Query knnQuery = Query.of(n -> n.knn(kkk));
		return knnQuery;
	
	}

	private List<String> getSearchableFields(Class<?> entityClass) {
		return searchableFieldsCache.computeIfAbsent(entityClass, clazz -> {
			log.debug("Reflecting @SearchableField annotations for class: {}", clazz.getSimpleName());
			return Arrays.stream(clazz.getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(SearchableField.class)).map(Field::getName)
					.collect(Collectors.toList());
		});
	}

	private Optional<String> getGeoField(Class<?> entityClass) {
		// We expect only one @GeoPointField per entity
		String fieldName = geoFieldCache.computeIfAbsent(entityClass, clazz -> {
			log.debug("Reflecting @GeoPointField annotation for class: {}", clazz.getSimpleName());
			return Arrays.stream(clazz.getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(GeoPointField.class)).map(Field::getName).findFirst()
					.orElse(null);
		});
		return Optional.ofNullable(fieldName);
	}
}