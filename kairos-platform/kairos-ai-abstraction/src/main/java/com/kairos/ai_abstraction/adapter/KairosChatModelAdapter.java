package com.kairos.ai_abstraction.adapter;

import com.kairos.core.ai.ChatLanguageModel;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;

/**
 * Adapts the generic LangChain4j ChatLanguageModel (provided by Spring Boot starters)
 * to the KAIROS specific ChatLanguageModel interface.
 */
@RequiredArgsConstructor
public class KairosChatModelAdapter implements ChatLanguageModel {

    // This is the generic interface from LangChain4j
    private final ChatModel delegate;

	@Override
	public ChatModel getModel() {
		return delegate;
	}

    

   
}