package com.kairos.ingestion.model;

import java.util.UUID;

import com.kairos.ingestion.model.DocumentNode.NodeType;

import lombok.Data;

@Data
public class DocumentTree {
    private String documentId; // e.g., "DOC-123"
    private String sourceName; // e.g., "financial_report.pdf"
    private DocumentNode rootNode;

    public DocumentTree(String sourceName) {
        this.documentId = UUID.randomUUID().toString();
        this.sourceName = sourceName;
        // The root is always a virtual container for the whole doc
        this.rootNode = new DocumentNode(NodeType.ROOT, "ROOT", 0);
    }
}
