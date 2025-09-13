package com.kairos.sports_atlas.tools;

import org.springframework.stereotype.Component;

import com.kairos.agentic_framework.config.ConversationIdProvider;
import com.kairos.sports_atlas.util.Util;

@Component
public class LoggedUserConversationIdProvider implements ConversationIdProvider{

	@Override
	public String getConversationId() {
		return Util.getConversationId();
	}

}
