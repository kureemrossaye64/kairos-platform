package com.kairos.autoconfigure.diagnostics;

import com.kairos.core.ai.ChatLanguageModel;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class MissingChatModelFailureAnalyzer extends AbstractFailureAnalyzer<NoSuchBeanDefinitionException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, NoSuchBeanDefinitionException cause) {
        // If the missing bean is our Core ChatLanguageModel
        if (ChatLanguageModel.class.equals(cause.getBeanType())) {
            return new FailureAnalysis(
                "KAIROS could not find a configured AI Chat Model.",
                "KAIROS relies on LangChain4j starters. Please ensure you have added a provider starter " +
                "and its required properties.\n\n" +
                "1. For Google Gemini (Default recommended):\n" +
                "   Dependency: langchain4j-vertex-ai-gemini-spring-boot-starter\n" +
                "   Properties: langchain4j.vertex-ai-gemini.project-id, .location, .chat-model.model-name\n\n" +
                "2. For OpenAI:\n" +
                "   Dependency: langchain4j-open-ai-spring-boot-starter\n" +
                "   Properties: langchain4j.open-ai.chat-model.api-key\n",
                cause
            );
        }
        return null;
    }
}