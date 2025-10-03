package com.kairos.core.ingestion;

import java.util.Map;

import com.kairos.core.BaseObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SourceRecord extends BaseObject {

    private String storageUri;

    private String sourceName;

    private String contentType;

    private ProcessingStatus status = ProcessingStatus.PENDING;

    private Map<String, Object> metadataManifest;

    private String failureReason;
}