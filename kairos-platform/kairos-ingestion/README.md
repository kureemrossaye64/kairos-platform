# kairos-ingestion

The `kairos-ingestion` module is responsible for the end-to-end data processing pipeline that transforms raw data into searchable knowledge.

## Overview

The ingestion pipeline handles document parsing, cleaning, splitting, and enrichment. It is designed to process various file types and prepare them for the vector store.

## Key Features

- **Multi-Format Parsing**: Uses **Apache Tika** to extract text and metadata from PDFs, Word docs, Excel files, and more.
- **Intelligent Splitting**: Supports multiple splitting strategies, including:
  - Recursive character splitting.
  - Parent-Child splitting for better context preservation.
  - Hierarchical splitting.
- **AI Enrichment**: Automatically enriches documents using LLMs during the ingestion process (e.g., generating summaries, extracting entities).
- **Multi-Modal Ingestion**: Specialized processors for images, audio (transcription), and video analysis.
- **Pipeline Orchestration**: A flexible pipeline architecture that allows for custom processors and sinks.

## Technologies

- **Apache Tika**: For document parsing.
- **LangChain4j**: For AI-driven enrichment.
- **Spring Boot**: For service orchestration.

## Configuration

```properties
# File ingestion properties
kairos.ingestion.file.upload-dir=/tmp/kairos/uploads
kairos.ingestion.file.max-size=10MB
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-ingestion</artifactId>
    <version>${project.version}</version>
</dependency>
```
