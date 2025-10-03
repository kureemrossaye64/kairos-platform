package com.kairos.sports_atlas.events;

import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.kairos.core.search.Searchable;
import com.kairos.core.search.VectorStoreService;
import com.kairos.sports_atlas.entities.BaseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EntityIndexingListener implements ApplicationListener<EntityChangeEvent>{

	private final VectorStoreService searchService;

	public void handleSearchableEntityChangeEvent(EntityChangeEvent event) {
		List<BaseEntity> entities = event.getEntity();
		if (entities.size() == 0) {
			return;
		}

		Class<?> entityClass = entities.get(0).getClass();

		if (!entityClass.isAnnotationPresent(Searchable.class)) {
			return;
		}

		String indexName = entityClass.getAnnotation(Searchable.class).indexName();

		for (BaseEntity entity : entities) {

			log.info("Received event type '{}' for @Searchable entity ID {}. Processing for index '{}'.",
					event.getType(), entity.getId(), indexName);

			if (event.getType() == EntityChangeEvent.ChangeType.DELETED) {
				searchService.delete(entityClass, entity.getId());
			} else {
				searchService.index(entity.getId(), entity);
			}
		}
	}

	public void onApplicationEvent(EntityChangeEvent event) {
		handleSearchableEntityChangeEvent(event);
		
	}

}