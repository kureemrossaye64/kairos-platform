package com.kairos.sports_atlas.facility.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.search.SearchQuery;
import com.kairos.core.search.SearchResult;
import com.kairos.core.search.VectorStoreService;
import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.BaseEntity;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.repositories.ServiceEntityRepository;
import com.kairos.sports_atlas.services.Manifestable;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceEntityService extends GenericCrudService<ServiceEntity, ServiceEntityRepository> {

    private final VectorStoreService searchService;

    public ServiceEntityService(ServiceEntityRepository repository, VectorStoreService searchService) {
        super(repository);
        this.searchService = searchService;
    }

    /**
     * Creates or updates the public-facing service manifest for a given operational entity.
     * This is the core of the "manifestation" process.
     * @param sourceEntity An entity that implements the Manifestable interface.
     */
    @Transactional
    public void manifestService(Manifestable sourceEntity) {
        if (sourceEntity == null || !(sourceEntity instanceof BaseEntity)) {
            log.error("Cannot manifest a null or non-BaseEntity object.");
            return;
        }

        // Check if a service manifest already exists for this entity
        ServiceEntity service = repository.findByOriginEntityId(((BaseEntity) sourceEntity).getId())
                .orElse(new ServiceEntity()); // If not, create a new one

        // Use the entity's own logic to populate/update the manifest
        service = sourceEntity.toServiceEntity();

        log.info("Manifesting service '{}' from origin {}:{}", service.getName(), service.getOriginEntityType(), service.getOriginEntityId());
        
        // Saving the service will automatically trigger the EntityChangeEvent<ServiceEntity>
        // which our listener will pick up for indexing in OpenSearch.
        this.save(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> findServices(SearchQuery query) {
        SearchResult result = searchService.search( query, ServiceEntity.class);
        if (result.totalHits() == 0) {
            return Collections.emptyList();
        }
        return repository.findAllById(result.documentIds());
    }
}