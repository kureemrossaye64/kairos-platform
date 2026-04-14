package com.kairos.core.ai;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Defines the contract for a chat-based language model service.
 * This interface decouples the application from the specific implementation (e.g., Vertex AI, OpenAI).
 *
 * Design choice: Instead of creating a completely new interface, we extend LangChain4j's
 * interface. This gives us the benefit of abstraction while allowing us to seamlessly
 * plug our implementation into other LangChain4j components like AiServices later.
 */
public interface ChatLanguageModel  {
	
	
    public ChatModel getModel();
    
}