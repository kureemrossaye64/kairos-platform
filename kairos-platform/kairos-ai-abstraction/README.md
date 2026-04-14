# kairos-ai-abstraction

The `kairos-ai-abstraction` module serves as the bridge between the KAIROS platform and various AI models. It leverages [LangChain4j](https://github.com/langchain4j/langchain4j) to provide a unified interface for interacting with Large Language Models (LLMs) and Embedding models.

## Overview

This module implements the AI interfaces defined in `kairos-core`. It provides adapters that translate Kairos-specific requests into LangChain4j calls, allowing the platform to support a wide range of AI providers.

## Key Features

- **Multi-Provider Support**: Supports all AI providers compatible with LangChain4j (OpenAI, Anthropic, Google Gemini, Ollama, etc.).
- **Multi-Modal Analysis**: Specialized services for analyzing:
  - **Text**: Summarization, sentiment analysis, and extraction.
  - **Images**: Object detection and descriptive analysis.
  - **Audio**: Transcription and content analysis.
  - **Video**: Scene detection and summary.
- **Unified Embedding Interface**: Seamless integration with embedding models for RAG workflows.

## Technologies

- **LangChain4j**: The core library for AI model integration.
- **Java 17**

## Configuration

The AI models are typically configured via Spring Boot properties when using the `kairos-autoconfigure` module.

```properties
# Example configuration (depends on the provider)
kairos.ai.chat-model.provider=openai
kairos.ai.chat-model.api-key=${OPENAI_API_KEY}
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-ai-abstraction</artifactId>
    <version>${project.version}</version>
</dependency>
```
