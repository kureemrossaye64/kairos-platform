# kairos-autoconfigure

The `kairos-autoconfigure` module provides Spring Boot auto-configuration for the entire KAIROS platform.

## Overview

This module follows the standard Spring Boot auto-configuration pattern. It detects the presence of Kairos modules on the classpath and automatically configures the necessary beans based on properties.

## Key Features

- **Zero-Config Setup**: Automatically bootstraps Kairos services with sensible defaults.
- **Conditional Configuration**: Only configures components when their dependencies are available (e.g., only configures pgvector if the postgres driver is present).
- **Centralized Property Management**: Defines all `kairos.*` configuration properties in one place.
- **Diagnostic Failures**: Includes failure analyzers to provide helpful error messages when required AI models or configurations are missing.

## Configuration

Annotate your main application class with `@EnableKairos` to activate the platform.

```java
@SpringBootApplication
@EnableKairos
public class MyKairosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyKairosApplication.class, args);
    }
}
```

## Usage

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-autoconfigure</artifactId>
    <version>${project.version}</version>
</dependency>
```
