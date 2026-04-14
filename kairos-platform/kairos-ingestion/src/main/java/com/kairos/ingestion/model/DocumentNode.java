package com.kairos.ingestion.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(exclude = {"parent", "children"}) // Prevent StackOverflow in logs
public class DocumentNode {
    
    public enum NodeType { ROOT, SECTION, CONTENT, TABLE, IMAGE_DESC }

    private String id;
    private NodeType type;
    private String titleOrContent; // Header text (if Section) or Body text (if Content)
    private int level; // 0=Root, 1=H1, 2=H2, etc. (Content usually inherits parent level)
    
    private Map<String, Object> metadata = new HashMap<>();
    
    private DocumentNode parent;
    private List<DocumentNode> children = new ArrayList<>();

    public DocumentNode(NodeType type, String content, int level) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.titleOrContent = content;
        this.level = level;
    }

    public void addChild(DocumentNode child) {
        child.setParent(this);
        this.children.add(child);
    }
    
    // Helper to get text for Embedding
    public String getFullContextPath() {
        if (parent == null || parent.getType() == NodeType.ROOT) return "";
        String parentPath = parent.getFullContextPath();
        return (parentPath.isEmpty() ? "" : parentPath + " > ") + 
               (this.type == NodeType.SECTION ? this.titleOrContent : "");
    }
}
