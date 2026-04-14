# kairos-vector-search

The `kairos-vector-search` module provides the implementation for vector storage and retrieval, which is essential for Retrieval-Augmented Generation (RAG) applications.

## Overview

This module handles the storage of document embeddings and performs hybrid searches (combining vector similarity with keyword filtering). It abstracts the underlying vector database, allowing for flexible deployments.

## Key Features

- **Hybrid Retrieval**: Combines semantic vector search with traditional filtering for more accurate results.
- **Multiple Backend Support**:
  - **PostgreSQL (pgvector)**: Leverages the `pgvector` extension for efficient vector operations within a relational database.
  - **Qdrant**: Integration with the Qdrant vector database for high-performance similarity search.
  - **In-Memory**: A simple in-memory implementation for testing and small-scale applications.
- **Dynamic Query Building**: Advanced query builders for constructing complex search requests.

## Technologies

- **PostgreSQL / pgvector**
- **Qdrant Java Client**
- **LangChain4j (Embeddings)**
- **Spring Data JPA**

## Configuration

Configure your vector store using properties:

```properties
# For PostgreSQL / pgvector
kairos.vector-store.type=postgres
kairos.vector-store.postgres.table-name=kairos_embeddings
kairos.vector-store.postgres.dimension=1536
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-vector-search</artifactId>
    <version>${project.version}</version>
</dependency>
```
