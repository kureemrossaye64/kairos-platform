# kairos-storage

The `kairos-storage` module provides an abstraction layer for file storage, allowing the platform to store and retrieve documents across different storage providers.

## Overview

This module defines a common `StorageService` interface and provides implementations for local and cloud-based storage.

## Key Features

- **Provider Abstraction**: Switch between storage providers without changing application logic.
- **Local Storage**: Simple filesystem-based storage for development and on-premise deployments.
- **Google Cloud Storage (GCS)**: Integrated support for GCS for scalable, cloud-native storage.
- **Secure File Handling**: Methods for storing, retrieving, and deleting files with ease.

## Technologies

- **Google Cloud Storage SDK**
- **Spring Boot**

## Configuration

```properties
# For Local Storage
kairos.storage.type=local
kairos.storage.local.path=/var/lib/kairos/storage

# For GCS
kairos.storage.type=gcs
kairos.storage.gcs.bucket-name=my-kairos-bucket
kairos.storage.gcs.project-id=my-gcp-project
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-storage</artifactId>
    <version>${project.version}</version>
</dependency>
```
