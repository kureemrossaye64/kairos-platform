package com.kairos.sports_atlas.tools;

import org.springframework.stereotype.Component;

import com.kairos.core.agentic.ConversationContexProvider;
import com.kairos.sports_atlas.util.Util;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

@Component
public class LoggedUserConversationContextProvider implements ConversationContexProvider{

	@Override
	public String getConversationId() {
		return Util.getConversationId();
	}

	@Override
	public ChatMemory getChatMemory(String sessionId) {
		return MessageWindowChatMemory.withMaxMessages(20);
	}

}
