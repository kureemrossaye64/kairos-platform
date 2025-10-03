package com.kairos.sports_atlas.entities;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import com.kairos.core.ingestion.ProcessingStatus;
import com.kairos.core.ingestion.SourceRecord;
import com.kairos.sports_atlas.entities.BaseEntity;

import java.util.Map;

@Entity
@Table(name = "source_records")
@Getter
@Setter
@NoArgsConstructor
public class JpaSourceRecord extends BaseEntity {

    @Column(nullable = false, unique = true, length = 1024)
    private String storageUri;

    @Column(nullable = false)
    private String sourceName;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.PENDING;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadataManifest;

    @Column(columnDefinition = "TEXT")
    private String failureReason;
    
    
    public static JpaSourceRecord from(SourceRecord i) {
    	JpaSourceRecord o = new JpaSourceRecord();
    	o.setContentType(i.getContentType());
    	o.setCreatedAt(i.getCreatedAt());
    	o.setFailureReason(i.getFailureReason());
    	o.setId(i.getId());
    	o.setMetadataManifest(i.getMetadataManifest());
    	o.setSourceName(i.getSourceName());
    	o.setStatus(i.getStatus());
    	o.setStorageUri(i.getStorageUri());
    	o.setUpdatedAt(i.getUpdatedAt());
    	return o;
    	
    	
    }
    
    public SourceRecord toDto() {
    	SourceRecord o = new SourceRecord();
    	o.setContentType(getContentType());
    	o.setCreatedAt(getCreatedAt());
    	o.setFailureReason(getFailureReason());
    	o.setId(getId());
    	o.setMetadataManifest(getMetadataManifest());
    	o.setSourceName(getSourceName());
    	o.setStatus(getStatus());
    	o.setStorageUri(getStorageUri());
    	o.setUpdatedAt(getUpdatedAt());
    	return o;
    }
}