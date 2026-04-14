package com.kairos.ingestion.graph.spi;



import java.util.List;

import com.kairos.ingestion.graph.model.GraphNodeInfo;
import com.kairos.ingestion.graph.model.GraphView;
import com.kairos.ingestion.model.DocumentTree;

public interface KnowledgeGraphRepository {

    // --- READ / NAVIGATION ---

    /**
     * Entry point for the AI. Returns the top-level "Universe" nodes.
     */
    List<GraphNodeInfo> getRootNodes();

    /**
     * Returns the context of a specific node (Focus + Neighbors).
     */
    GraphView getNodeContext(String nodeId);

    /**
     * Checks if a node exists.
     */
    boolean exists(String nodeId);

    // --- WRITE / MUTATION ---

    /**
     * Creates a high-level grouping node (e.g., "Finance", "Legal").
     */
    String createTopicNode(String name, String description, String parentId);

    /**
     * Takes the parsed DocumentTree (from Step 1) and explodes it into the graph.
     * It links the Document Root to the specified parentNodeId.
     */
    void attachDocument(DocumentTree document, String parentNodeId, String relationType);

    // --- PERSISTENCE ---
    
    /**
     * Saves the in-memory graph to disk (JSON).
     */
    void saveSnapshot();

    /**
     * Loads the graph from disk on startup.
     */
    void loadSnapshot();
}