package com.kairos.sports_atlas.entities;

import com.kairos.core.entity.BaseEntity;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.Searchable;
import com.kairos.search.annotation.SearchableField;
import com.kairos.search.model.VectorSearcheable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@Searchable(indexName = "services") // The single, unified search index
public class ServiceEntity extends BaseEntity implements VectorSearcheable {

    @Column(nullable = false)
    @SearchableField
    private String name;

    @Column(columnDefinition = "TEXT")
    @SearchableField
    private String description;

    @Column(nullable = false)
    @SearchableField
    private String location;

    @Column(columnDefinition = "geography(Point, 4326)")
    @GeoPointField
    private Point coordinates;

    @Column(nullable = false)
    @SearchableField
    private String serviceType;

    @Column(nullable = false)
    private boolean isBookable = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipType ownershipType;

    @Column(nullable = false)
    private UUID originEntityId;

    @Column(nullable = false)
    private String originEntityType;
    
    private transient float[] textEmbedding;
}
