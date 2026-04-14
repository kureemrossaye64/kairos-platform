package com.kairos.pulsberry.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kairos.core.agentic.ConversationContexProvider;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

@Component
public class UserContextProvider implements ConversationContexProvider{
	
	public final static ThreadLocal<String> CONVERSATION_ID = new ThreadLocal<String>();
	
	@Autowired
	private PersistentChatMemoryStore store;

	@Override
	public String getConversationId() {
		return CONVERSATION_ID.get();
	}

	@Override
	public ChatMemory getChatMemory(String sessionId) {
		ChatMemory chatMemory = MessageWindowChatMemory.builder()
			    .id(sessionId) // Unique ID for the conversation
			    .maxMessages(10) // Retain the last 10 messages in the context window
			    .chatMemoryStore(store) // Plug in your custom store
			    .build();
		return chatMemory;
	}

}
