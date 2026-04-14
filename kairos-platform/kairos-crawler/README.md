# kairos-crawler

The `kairos-crawler` module is a high-performance web crawler designed to feed data directly into the KAIROS ingestion pipeline.

## Overview

Based on a custom implementation that handles `robots.txt` compliance, concurrent fetching, and intelligent parsing, the crawler allows for automated knowledge gathering from the web.

## Key Features

- **Concurrent Crawling**: Multi-threaded execution for high throughput.
- **Robots.txt Compliance**: Respects website crawling rules and politeness policies.
- **Deep Link Extraction**: Automatically discovers and follows links based on configurable depth and patterns.
- **Tika Integration**: Integrated with Apache Tika for extracting content from various web formats (HTML, PDF, etc.).
- **Distributed Ready**: Interfaces for distributed job scheduling.
- **Persistence**: Tracks crawl jobs and status using the platform's core persistence services.

## Technologies

- **Apache HttpClient**: For web fetching.
- **Apache Tika**: For content parsing.
- **Spring Boot**: For service management.

## Configuration

```properties
kairos.crawler.user-agent=KairosBot/1.0
kairos.crawler.max-pages=1000
kairos.crawler.max-depth=3
kairos.crawler.politeness-delay=1000
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-crawler</artifactId>
    <version>${project.version}</version>
</dependency>
```
