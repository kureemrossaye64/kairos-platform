package com.kairos.sports_atlas.governance;

import com.kairos.agentic_framework.governance.InsightModule;
import org.springframework.stereotype.Component;

@Component
public class UnmetNeedsModule implements InsightModule {
    @Override
    public String getModuleName() { return "Top Unmet Community Needs"; }

    @Override
    public String getSystemPromptForAnalysis() {
        return "You are a senior policy analyst for a Ministry of Sports. Your task is to analyze the provided chat logs, where each entry is separated by '---'. " +
               "Your goal is to identify and summarize the top 3-5 most frequent 'unmet needs'. An unmet need is a user's request for a facility or service that the system could not fulfill, often indicated by the 'Unmet Need Flag: true' or phrases like 'I couldn't find'. " +
               "For each need, identify the requested activity (e.g., Tennis, Football) and the geographical location/district (e.g., Curepipe, Rivière du Rempart). " +
               "Synthesize these findings into a concise, structured report. For each identified need, provide a brief 'Recommendation' for the Ministry. " +
               "If no significant unmet needs are found, state that clearly.";
    }

    @Override
    public String processAnalysisResult(String analysisText) {
        // We can add formatting here later, like converting markdown to HTML.
        return analysisText;
    }
}