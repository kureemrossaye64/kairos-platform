package com.kairos.core.search;

import lombok.Builder;
import lombok.Value;
import java.util.Map;
import java.util.UUID;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

/**
 * A generic, domain-agnostic representation of a document to be indexed in the vector store.
 * This class is an immutable value object.
 *
 * @param id A unique identifier for this document.
 * @param content The actual text content to be embedded and indexed.
 * @param metadata A flexible map for storing any additional, filterable data
 *                 (e.g., source file name, associated entity ID, creation date).
 */
@Value
@Builder
public class VdbDocument {
    UUID id;
    String content;
    Map<String, Object> metadata;
    
    
    public TextSegment toTextSegment() {
    	TextSegment segment = TextSegment.from(content, toLangChainMetadata(this));
    	return segment;
    }
    
    private Metadata toLangChainMetadata(VdbDocument document) {
        Metadata metadata = new Metadata();
        document.getMetadata().forEach((key, value) -> metadata.put(key, value.toString()));
        metadata.put("internal_id", document.getId().toString());
        metadata.put("entity", "vsearch");
        return metadata;
    }
}