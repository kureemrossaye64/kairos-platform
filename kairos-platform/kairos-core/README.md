# kairos-core

The `kairos-core` module provides the foundational interfaces and domain models for the KAIROS platform. It defines the "contract" that other modules implement or consume, ensuring consistency across the entire ecosystem.

## Overview

This module is designed to be a lightweight dependency that contains the core abstractions for:
- **Search & Retrieval**: Interfaces for vector stores and searchable documents.
- **AI Services**: Abstractions for chat models, embedding models, and multi-modal analysis (audio, image, video).
- **Ingestion**: Models for ingestion requests and routing.
- **Crawling**: Definitions for crawl jobs, status tracking, and crawled content.
- **Storage**: Generic storage service interfaces.

## Key Features

- **Standardized Domain Models**: Unified representation of documents, search results, and ingestion statuses.
- **Extensible Abstractions**: Interfaces that allow for multiple implementations of core services (e.g., different vector stores or storage providers).
- **Lightweight**: Minimal external dependencies, making it easy to include in any Kairos-based project.

## Technologies

- **Java 17**
- **Jakarta Persistence (JPA)**: For base entity definitions.

## Usage

Most Kairos modules depend on `kairos-core`. When building a new component for the platform, you should use the interfaces defined here to ensure interoperability.

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-core</artifactId>
    <version>${project.version}</version>
</dependency>
```
