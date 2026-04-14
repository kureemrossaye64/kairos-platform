package com.kairos.autoconfigure;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.kairos.ai_abstraction.adapter.KairosChatModelAdapter;
import com.kairos.core.ai.AudioAnalysisService;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ai.ImageAnalysisService;
import com.kairos.core.ai.TextAnalysisService;
import com.kairos.core.ai.VideoAnalysisService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auto-configuration for AI and LLM components.
 * <p>
 * Configures Gemini models with sensible defaults. In dev mode with missing
 * project ID, provides mock implementations to allow startup without GCP credentials.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "kairos.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KairosAIAutoConfiguration {

    @Bean(name = "pro")
    @Primary
    public ChatLanguageModel proChatModel(Map<String, ChatModel> models) {
        
        // 1. Explicit named bean
        if (models.containsKey("proModel")) {
            log.info("KAIROS: Using explicit 'proModel' bean for Pro tasks.");
            return new KairosChatModelAdapter(models.get("proModel"));
        }

        // 2. Fallback to Primary/Single available model
        if (!models.isEmpty()) {
            // If there is only one, or a @Primary one, Spring injects it preferentially? 
            // Actually, in a Map, we get all. Let's pick the one that is likely Primary 
            // or just the first one if simpler.
            // For robustness, we rely on the fact that if the user didn't define "proModel",
            // they likely just used a Starter which provides one bean.
            ChatModel defaultModel = models.values().iterator().next();
            log.info("KAIROS: No explicit 'proModel' found. Aliasing default model for Pro tasks.");
            return new KairosChatModelAdapter(defaultModel);
        }

        throw new IllegalStateException("No ChatLanguageModel found. Please add a LangChain4j starter (e.g., langchain4j-vertex-ai-gemini-spring-boot-starter).");
    }

    @Bean(name = "cost-effective")
    public ChatLanguageModel costEffectiveChatModel(
            Map<String, ChatModel> models) {

        if (models.containsKey("fastModel")) {
            log.info("KAIROS: Using explicit 'fastModel' bean for Cost-Effective tasks.");
            return new KairosChatModelAdapter(models.get("fastModel"));
        }

        log.info("KAIROS: No explicit 'fastModel' found. Using Pro model for Cost-Effective tasks (Single-Model Mode).");
        if (!models.isEmpty()) {
            // If there is only one, or a @Primary one, Spring injects it preferentially? 
            // Actually, in a Map, we get all. Let's pick the one that is likely Primary 
            // or just the first one if simpler.
            // For robustness, we rely on the fact that if the user didn't define "proModel",
            // they likely just used a Starter which provides one bean.
            ChatModel defaultModel = models.values().iterator().next();
            log.info("KAIROS: No explicit 'proModel' found. Aliasing default model for Pro tasks.");
            return new KairosChatModelAdapter(defaultModel);
        }
        
        throw new IllegalStateException("No ChatLanguageModel found. Please add a LangChain4j starter (e.g., langchain4j-vertex-ai-gemini-spring-boot-starter).");
    }

    

    // DEV MODE: Mock LLM for development without GCP credentials
    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    @ConditionalOnProperty(prefix = "kairos", name = "dev-mode", havingValue = "true")
    public ChatModel mockNativeModel() {
        log.warn("⚠️ KAIROS: No AI Provider found. Creating Mock LLM for dev mode.");
        return new DisabledChatModel();
        // Note: You might want a simple logging implementation here instead of Disabled
    }

    @Bean
    @ConditionalOnMissingBean(ChatMemoryStore.class)
    public ChatMemoryStore chatMemoryStore() {
        // Default in-memory store
        return new ChatMemoryStore() {
            private final java.util.Map<Object, List<dev.langchain4j.data.message.ChatMessage>> 
                store = new java.util.HashMap<>();
            
            @Override
            public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId) {
                return store.getOrDefault(memoryId, new java.util.ArrayList<>());
            }
            
            @Override
            public void updateMessages(Object memoryId, List<dev.langchain4j.data.message.ChatMessage> messages) {
                store.put(memoryId, messages);
            }

			@Override
			public void deleteMessages(Object memoryId) {
				store.remove(memoryId);
				
			}
        };
    }
    
    @Bean
    @ConditionalOnMissingBean
    public AudioAnalysisService audioAnalysisService(@Qualifier("pro") ChatLanguageModel model) {
        return new com.kairos.ai_abstraction.service.impl.LLMAudioAnalysisService(model);
    }

    @Bean
    @ConditionalOnMissingBean
    public VideoAnalysisService videoAnalysisService(@Qualifier("pro") ChatLanguageModel model) {
        return new com.kairos.ai_abstraction.service.impl.LLMVideoAnalysisService(model);
    }

    @Bean
    @ConditionalOnMissingBean
    public ImageAnalysisService imageAnalysisService(@Qualifier("pro") ChatLanguageModel model) {
        return new com.kairos.ai_abstraction.service.impl.LLMImageAnalysisService(model);
    }

    @Bean
    @ConditionalOnMissingBean
    public TextAnalysisService textAnalysisService(@Qualifier("pro") ChatLanguageModel model) {
        return new com.kairos.ai_abstraction.service.impl.LLMTextAnalysisService(model);
    }
}