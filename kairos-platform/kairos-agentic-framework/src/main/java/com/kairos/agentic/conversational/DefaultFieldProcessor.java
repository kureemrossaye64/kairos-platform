package com.kairos.agentic.conversational;

import org.springframework.stereotype.Component;

/**
 * The default, do-nothing processor. It simply accepts the raw string input.
 */
@Component
public class DefaultFieldProcessor implements FieldProcessorStrategy {
    @Override
    public ProcessingResult process(String rawInput) {
        return ProcessingResult.success(rawInput);
    }
}