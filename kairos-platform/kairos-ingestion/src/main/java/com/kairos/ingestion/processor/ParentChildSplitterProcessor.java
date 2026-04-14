package com.kairos.ingestion.processor;

import java.util.stream.Stream;

import com.kairos.ingestion.pipeline.Processor;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParentChildSplitterProcessor implements Processor<Document, TextSegment> {

    // Parent chunks: Large (e.g., 1000 chars) -> Used for LLM Context
    // Child chunks: Small (e.g., 300 chars) -> Used for Vector Search
    private final int parentSize = 1200;
    private final int childSize = 400;
    private final int overlap = 50;

    @Override
    public Stream<TextSegment> process(Stream<Document> inputStream) {
        return inputStream.flatMap(doc -> {
            // 1. Split into large Parent segments first
            var parentSplitter = DocumentSplitters.recursive(parentSize, 0);
            var parents = parentSplitter.split(doc);

            return parents.stream().flatMap(parentSegment -> {
                String parentText = parentSegment.text();
                
                // 2. Split Parent into smaller Child segments
                var childSplitter = DocumentSplitters.recursive(childSize, overlap);
                var children = childSplitter.split(Document.from(parentText, parentSegment.metadata()));

                // 3. Attach Parent Text to Child Metadata
                return children.stream().map(child -> {
                    child.metadata().put("parent_content", parentText);
                    return child;
                });
            });
        });
    }
}
