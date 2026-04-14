package com.kairos.ingestion.graph.impl;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.ingestion.graph.model.GraphNodeInfo;
import com.kairos.ingestion.graph.model.GraphView;
import com.kairos.ingestion.graph.spi.KnowledgeGraphRepository;
import com.kairos.ingestion.model.DocumentNode;
import com.kairos.ingestion.model.DocumentTree;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JGraphTRepository implements KnowledgeGraphRepository {

    private final ObjectMapper objectMapper;

    
    private final String storagePath;

    // The In-Memory Graph
    private Graph<InternalVertex, LabeledEdge> graph;

    // --- INTERNAL DATA STRUCTURES ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternalVertex {
        private String id;
        private String label;
        private String type;    // TOPIC, DOCUMENT, SECTION
        private String summary; // Metadata or short text
        
        // We do NOT store full content here. 
        // In a real app, ID maps to a Blob Store / Vector DB.
    }

    public static class LabeledEdge extends DefaultEdge {
        private static final long serialVersionUID = 1L;
		private String label; // "HAS_CHILD", "RELATED_TO"
        
        public LabeledEdge() { this.label = "RELATED_TO"; }
        public LabeledEdge(String label) { this.label = label; }
        
        public String getLabel() { return label; }
    }

    // --- LIFECYCLE ---

    @PostConstruct
    public void init() {
        // Initialize empty graph
        this.graph = new DefaultDirectedGraph<>(LabeledEdge.class);
        loadSnapshot(); // Try to load from disk
    }

    @PreDestroy
    public void shutdown() {
        saveSnapshot(); // Save on exit
    }

    // --- NAVIGATION IMPLEMENTATION ---

    @Override
    public synchronized List<GraphNodeInfo> getRootNodes() {
        // In this implementation, "Roots" are Topic nodes that have incoming edges only from "UNIVERSE"
        // Or simply, we define a constant "ROOT" node.
        if (!graph.containsVertex(new InternalVertex("ROOT", "", "", ""))) {
            return new ArrayList<>();
        }
        return getNeighborsMapped("ROOT");
    }

    @Override
    public synchronized GraphView getNodeContext(String nodeId) {
        InternalVertex focus = findVertexById(nodeId);
        if (focus == null) throw new IllegalArgumentException("Node not found: " + nodeId);

        List<GraphNodeInfo> neighbors = getNeighborsMapped(nodeId);

        return GraphView.builder()
                .focusNode(mapToInfo(focus))
                .neighbors(neighbors)
                .build();
    }

    @Override
    public boolean exists(String nodeId) {
        return findVertexById(nodeId) != null;
    }

    // --- MUTATION IMPLEMENTATION ---

    @Override
    public synchronized String createTopicNode(String name, String description, String parentId) {
        String newId = "TOPIC-" + UUID.randomUUID().toString().substring(0, 8);
        
        InternalVertex newTopic = InternalVertex.builder()
                .id(newId)
                .label(name)
                .type("TOPIC")
                .summary(description)
                .build();
        
        graph.addVertex(newTopic);
        log.info("Created Topic Node: {}", name);

        // Link to parent
        if (parentId != null && exists(parentId)) {
            InternalVertex parent = findVertexById(parentId);
            graph.addEdge(parent, newTopic, new LabeledEdge("HAS_TOPIC"));
        }
        
        saveSnapshot(); // Auto-save for safety
        return newId;
    }

    @Override
    public synchronized void attachDocument(DocumentTree document, String parentNodeId, String relationType) {
        log.info("Attaching Document '{}' to Node '{}'", document.getSourceName(), parentNodeId);

        InternalVertex parent = findVertexById(parentNodeId);
        if (parent == null) throw new IllegalArgumentException("Parent node not found: " + parentNodeId);

        // 1. Create Document Root Vertex
        InternalVertex docVertex = InternalVertex.builder()
                .id(document.getDocumentId())
                .label(document.getSourceName())
                .type("DOCUMENT")
                .summary("Document imported via AI Ingestion")
                .build();
        
        graph.addVertex(docVertex);
        graph.addEdge(parent, docVertex, new LabeledEdge(relationType));

        // 2. Recursively add the Page Index (Sections)
        if (document.getRootNode() != null) {
            // The DocumentTree root is usually virtual, so we iterate its children (H1s)
            for (DocumentNode child : document.getRootNode().getChildren()) {
                addRecursiveTreeNodes(docVertex, child);
            }
        }
        
        saveSnapshot();
    }

    private void addRecursiveTreeNodes(InternalVertex parentVertex, DocumentNode docNode) {
        // We only map SECTIONs to the Graph to keep it navigable.
        // Content/Text is usually kept inside the Section node's metadata or Vector DB.
        
        if ("SECTION".equalsIgnoreCase(docNode.getType().name())) {
            InternalVertex sectionVertex = InternalVertex.builder()
                    .id(docNode.getId())
                    .label(docNode.getTitleOrContent()) // The Header
                    .type("SECTION")
                    .summary("Level " + docNode.getLevel())
                    .build();

            graph.addVertex(sectionVertex);
            graph.addEdge(parentVertex, sectionVertex, new LabeledEdge("HAS_SECTION"));

            // Recurse
            for (DocumentNode child : docNode.getChildren()) {
                addRecursiveTreeNodes(sectionVertex, child);
            }
        }
    }

    // --- PERSISTENCE (JSON) ---

    @Override
    public synchronized void saveSnapshot() {
        try {
            File file = new File(storagePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();

            GraphDto dto = new GraphDto();
            dto.setVertices(new ArrayList<>(graph.vertexSet()));
            
            List<EdgeDto> edges = new ArrayList<>();
            for (LabeledEdge e : graph.edgeSet()) {
                edges.add(new EdgeDto(
                        graph.getEdgeSource(e).getId(),
                        graph.getEdgeTarget(e).getId(),
                        e.getLabel()
                ));
            }
            dto.setEdges(edges);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dto);
            log.info("Graph snapshot saved to {}", storagePath);
        } catch (IOException e) {
            log.error("Failed to save graph snapshot", e);
        }
    }

    @Override
    public synchronized void loadSnapshot() {
        File file = new File(storagePath);
        if (!file.exists()) {
            log.info("No existing graph snapshot. Creating ROOT universe.");
            InternalVertex root = new InternalVertex("ROOT", "Universe", "ROOT", "The start of all knowledge.");
            graph.addVertex(root);
            return;
        }

        try {
            GraphDto dto = objectMapper.readValue(file, GraphDto.class);
            
            // Rebuild Graph
            for (InternalVertex v : dto.getVertices()) {
                graph.addVertex(v);
            }
            for (EdgeDto e : dto.getEdges()) {
                InternalVertex src = findVertexById(e.getSourceId());
                InternalVertex tgt = findVertexById(e.getTargetId());
                if (src != null && tgt != null) {
                    graph.addEdge(src, tgt, new LabeledEdge(e.getLabel()));
                }
            }
            log.info("Graph snapshot loaded: {} vertices, {} edges", graph.vertexSet().size(), graph.edgeSet().size());
        } catch (IOException e) {
            log.error("Failed to load graph snapshot", e);
        }
    }

    // --- HELPERS ---

    private InternalVertex findVertexById(String id) {
        return graph.vertexSet().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private List<GraphNodeInfo> getNeighborsMapped(String nodeId) {
        InternalVertex vertex = findVertexById(nodeId);
        if (vertex == null) return new ArrayList<>();

        return graph.outgoingEdgesOf(vertex).stream()
                .map(edge -> graph.getEdgeTarget(edge))
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    private GraphNodeInfo mapToInfo(InternalVertex v) {
        return GraphNodeInfo.builder()
                .id(v.getId())
                .label(v.getLabel())
                .type(v.getType())
                .summary(v.getSummary())
                .build();
    }

    // DTOs for Persistence
    @Data @NoArgsConstructor @AllArgsConstructor
    private static class GraphDto {
        private List<InternalVertex> vertices;
        private List<EdgeDto> edges;
    }
    @Data @NoArgsConstructor @AllArgsConstructor
    private static class EdgeDto {
        private String sourceId;
        private String targetId;
        private String label;
    }
}
