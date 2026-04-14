package com.kairos.ingestion.spi;

import com.kairos.ingestion.model.DocumentTree;
import java.io.InputStream;

public interface DocumentParser {
    /**
     * Parses an input stream into a semantic tree.
     * @param inputStream The file stream (PDF, DOCX, etc.)
     * @param fileName The name of the file for metadata.
     * @return A constructed DocumentTree.
     */
    DocumentTree parse(InputStream inputStream, String fileName);
}