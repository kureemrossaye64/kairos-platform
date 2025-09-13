package com.kairos.cultural_archive.entity;

import com.kairos.core.entity.BaseEntity;
import com.kairos.cultural_archive.model.AssetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "cultural_assets")
@Getter
@Setter
public class CulturalAsset extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    // The UUID of the primary text segment in the vector store.
    // This creates the crucial link between our relational metadata and the semantic data.
    @Column(name = "vector_store_id")
    private String vectorStoreId; 

    private String originalFileName;
    
    private String storageLocation; // e.g., a path in Google Cloud Storage
}