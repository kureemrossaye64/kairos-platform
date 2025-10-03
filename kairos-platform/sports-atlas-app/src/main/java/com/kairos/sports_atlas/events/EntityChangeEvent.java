package com.kairos.sports_atlas.events;

import lombok.Getter;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.kairos.sports_atlas.entities.BaseEntity;

@Getter
public class EntityChangeEvent extends ApplicationEvent {
    private final List<BaseEntity> entity;
    private final ChangeType type;

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED
    }

    /**
     * @param source The object on which the event initially occurred (e.g., the service).
     * @param entity The entity that was changed.
     * @param type The type of change.
     */
    public EntityChangeEvent(Object source, BaseEntity entity, ChangeType type) {
        super(source);
        this.entity = List.of( entity);
        this.type = type;
    }
    
    public EntityChangeEvent(Object source, List<BaseEntity> entity, ChangeType type) {
        super(source);
        this.entity = entity;
        this.type = type;
    }
}