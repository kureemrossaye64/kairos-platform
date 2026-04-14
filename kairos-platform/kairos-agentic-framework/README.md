# kairos-agentic-framework

The `kairos-agentic-framework` provides the infrastructure for building autonomous AI agents that can interact with users, use tools, and maintain conversational context.

## Overview

This module extends the basic LLM capabilities to create sophisticated agents. It includes support for tool-calling, conversational forms, and long-term memory.

## Key Features

- **Agent Factory**: Simplify the creation of AI agents with specific roles and instructions.
- **Conversational Forms**: Automatically generate interactive forms from Java objects, allowing agents to collect structured information from users through natural conversation.
- **Transactional Tools**: Infrastructure for tools that require transactional integrity (e.g., database updates).
- **Tool Specifications**: Easily define and expose custom tools to LLMs.
- **RAG Integration**: Built-in support for agents to query the Kairos vector search system.
- **Query Expansion & Ranking**: Advanced RAG techniques to improve the quality of agent responses.

## Technologies

- **LangChain4j (Agents & Tools)**
- **Spring Boot**

## Usage

Define an agent using the `AgentFactory` and register tools to empower it.

```java
@ConversationalEntity
public class UserRegistrationForm {
    @ConversationalField(description = "User's full name")
    private String name;

    @ConversationalField(description = "Email address")
    private String email;
}
```

```xml
<dependency>
    <groupId>com.kairos</groupId>
    <artifactId>kairos-agentic-framework</artifactId>
    <version>${project.version}</version>
</dependency>
```
