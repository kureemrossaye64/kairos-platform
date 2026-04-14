package com.kairos.ingestion.processor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.kairos.core.ingestion.SourceRecord;
import com.kairos.core.storage.StorageService;
import com.kairos.ingestion.model.DocumentNode;
import com.kairos.ingestion.model.DocumentTree;
import com.kairos.ingestion.pipeline.Processor;
import com.kairos.ingestion.service.LibrarianAgent;
import com.kairos.ingestion.spi.DocumentParser;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GraphIngestionProcessor implements Processor<SourceRecord, TextSegment> {

    private final StorageService storageService;
    private final DocumentParser documentParser; // Step 1 Interface
    private final LibrarianAgent librarianAgent; // Step 3 Service

    @Override
    public Stream<TextSegment> process(Stream<SourceRecord> inputStream) {
        return inputStream.flatMap(record -> {
            log.info("Processing Graph Ingestion for: {}", record.getSourceName());

            try (InputStream is = storageService.download(record.getStorageUri())) {
                
                // 1. PARSE: AI builds the Tree (Structure)
                DocumentTree tree = documentParser.parse(is, record.getSourceName());

                // 2. FILE: AI Librarian places the Tree into the Graph (Topology)
                // This updates the JGraphT in-memory graph.
                librarianAgent.fileDocumentInGraph(tree);

                // 3. FLATTEN: Convert Leaf Nodes to TextSegments for Vector DB (Content)
                // We link the Vector Record to the Graph Node via Metadata
                List<TextSegment> segments = new ArrayList<>();
                flattenTreeToSegments(tree.getRootNode(), record, segments);

                return segments.stream();

            } catch (Exception e) {
                log.error("Failed to ingest document into graph", e);
                // In production, handle DLQ (Dead Letter Queue) logic here
                return Stream.empty();
            }
        });
    }

    private void flattenTreeToSegments(DocumentNode node, SourceRecord record, List<TextSegment> collector) {
        // Only index leaf content (TEXT/TABLE), not structural headers (SECTION)
        // unless you want headers to be searchable too.
        if (node.getType() == DocumentNode.NodeType.CONTENT || 
            node.getType() == DocumentNode.NodeType.TABLE) {

            // The "Context Path" is crucial for RAG quality
            String contextPath = node.getFullContextPath(); 
            String fullText = String.format("Path: %s\nContent: %s", contextPath, node.getTitleOrContent());

            Metadata metadata = new Metadata(record.getMetadataManifest());
            metadata.put("source_filename", record.getSourceName());
            metadata.put("graph_node_id", node.getId()); // <--- THE LINK to JGraphT
            metadata.put("hierarchy_level", String.valueOf(node.getLevel()));
            metadata.put("page", String.valueOf(node.getMetadata().get("page")));

            collector.add(TextSegment.from(fullText, metadata));
        }

        // Recursion
        if (node.getChildren() != null) {
            for (DocumentNode child : node.getChildren()) {
                flattenTreeToSegments(child, record, collector);
            }
        }
    }
}