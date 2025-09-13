package com.kairos.sports_atlas.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SystemPromptService {

    @Value("classpath:prompts/kaya-system-prompt.txt")
    private Resource kayaPromptResource;

    private String kayaSystemPrompt;

    @PostConstruct
    public void init() {
        try {
            log.info("Loading Kaya system prompt...");
            this.kayaSystemPrompt = kayaPromptResource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Kaya system prompt loaded successfully.");
        } catch (IOException e) {
            log.error("FATAL: Could not load Kaya system prompt. The agent may not behave as expected.", e);
            this.kayaSystemPrompt = "You are a helpful assistant."; // Fallback
        }
    }

    public String getKayaSystemPrompt() {
        return this.kayaSystemPrompt;
    }
}