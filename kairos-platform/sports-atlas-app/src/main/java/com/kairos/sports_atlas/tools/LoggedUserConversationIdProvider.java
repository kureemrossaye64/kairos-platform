package com.kairos.sports_atlas.tools;

import org.springframework.stereotype.Component;

import com.kairos.core.agentic.ConversationIdProvider;
import com.kairos.sports_atlas.util.Util;

@Component
public class LoggedUserConversationIdProvider implements ConversationIdProvider{

	@Override
	public String getConversationId() {
		return Util.getConversationId();
	}

}
