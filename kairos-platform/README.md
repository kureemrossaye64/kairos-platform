# KAIROS Platform Backend

This directory contains the core Java backend for the KAIROS platform. It is built using Spring Boot and follows a modular Maven structure.

## Structure

The backend is organized into several modules, categorized as Core, Infrastructure, and Starters.

### Core Modules
- **kairos-core**: Defines the foundational interfaces and data models used across the platform.
- **kairos-ai-abstraction**: Provides a unified interface for AI models (LLMs, Embeddings) using LangChain4j.
- **kairos-ingestion**: Implements the RAG ingestion pipeline, including parsing, splitting, and AI-driven enrichment.
- **kairos-vector-search**: Handles vector storage and hybrid retrieval logic.
- **kairos-agentic-framework**: Provides the building blocks for creating autonomous AI agents.

### Infrastructure Modules
- **kairos-crawler**: A high-performance web crawler integrated with the Kairos ingestion system.
- **kairos-storage**: Abstracted file storage service supporting local filesystems and Google Cloud Storage (GCS).
- **kairos-notification**: A service for managing system notifications.
- **kairos-autoconfigure**: Contains Spring Boot auto-configuration logic for all Kairos components.

### Spring Boot Starters
We provide several starters to simplify the integration of Kairos into your Spring Boot applications:
- `kairos-spring-boot-starter-core`
- `kairos-spring-boot-starter-agent`
- `kairos-spring-boot-starter-vector`
- `kairos-spring-boot-starter-crawler`
- `kairos-spring-boot-starter-ingestion`

## Technologies

- **Java 17**
- **Spring Boot 3.5.x**
- **LangChain4j**: For LLM and Embedding model orchestration.
- **Maven**: For dependency management and build automation.
- **PostgreSQL / pgvector**: For relational and vector data storage.
- **Apache Tika**: For document parsing and content extraction.

## Build and Installation

To build the entire platform, run the following command from the root of the `kairos-platform` directory:

```bash
mvn clean install
```

This will build all modules and install them into your local Maven repository.

## Configuration

Each module can be configured using standard Spring Boot properties. Common configuration prefixes include:

- `kairos.*`: General platform settings.
- `kairos.vector-store.*`: Vector database configuration.
- `kairos.storage.*`: File storage settings.
- `kairos.crawler.*`: Web crawler parameters.

For detailed configuration options, refer to the individual module READMEs.

## License

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
