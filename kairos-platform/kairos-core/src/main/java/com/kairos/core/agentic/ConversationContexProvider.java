package com.kairos.core.agentic;

import dev.langchain4j.memory.ChatMemory;

public interface ConversationContexProvider {
	
	public String getConversationId();
	
	public ChatMemory getChatMemory(String conversationId);

}
