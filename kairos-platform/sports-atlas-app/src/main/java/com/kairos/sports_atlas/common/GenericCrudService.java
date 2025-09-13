package com.kairos.sports_atlas.common;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.core.entity.BaseEntity;
import com.kairos.sports_atlas.events.EntityChangeEvent;

/**
 * An abstract generic service providing basic CRUD operations.
 * All specific services (AthleteService, FacilityService) will extend this.
 * @param <T> The entity type, which must extend BaseEntity.
 * @param <R> The repository for the entity.
 */
public abstract class GenericCrudService<T extends BaseEntity, R extends JpaRepository<T, UUID>> {

    protected final R repository;
    private ApplicationEventPublisher eventPublisher;

    protected GenericCrudService(R repository) {
        this.repository = repository;
    }
    
    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Optional<T> findById(UUID id) {
        return repository.findById(id);
    }



    public List<T> findAll() {
        return repository.findAll();
    }

    public T save(T entity) {
        boolean isNew = entity.getId() == null;
        T savedEntity = repository.save(entity);

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new EntityChangeEvent(
                this, // The source of the event is this service
                savedEntity,
                isNew ? EntityChangeEvent.ChangeType.CREATED : EntityChangeEvent.ChangeType.UPDATED
            ));
        }
        return savedEntity;
    }
    
    public long count() {
    	return repository.count();
    }

    public void deleteById(UUID id) {
        // To publish a delete event, we need the entity.
        // We find it first, then delete it.
        repository.findById(id).ifPresent(entity -> {
            repository.deleteById(id);
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new EntityChangeEvent(
                    this,
                    entity,
                    EntityChangeEvent.ChangeType.DELETED
                ));
            }
        });
    }
    
    public List<T> saveAll(List<T> facilities) {
    	
    	List result = repository.saveAll(facilities);
    	if (eventPublisher != null) {
            eventPublisher.publishEvent(new EntityChangeEvent(
                this, // The source of the event is this service
                result,
                EntityChangeEvent.ChangeType.UPDATED
            ));
        }
		return result;
		
	}
}