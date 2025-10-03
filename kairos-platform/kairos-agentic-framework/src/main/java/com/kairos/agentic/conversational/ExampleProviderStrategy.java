package com.kairos.agentic.conversational;

import java.util.List;

/**
 * A strategy interface for dynamically providing example values for a conversational form field.
 */
public interface ExampleProviderStrategy {
    /**
     * @return A list of example valid answers for a field.
     */
    List<String> getExamples();
}