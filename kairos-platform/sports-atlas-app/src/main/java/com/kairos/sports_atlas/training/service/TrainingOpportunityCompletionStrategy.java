package com.kairos.sports_atlas.training.service;

import com.kairos.agentic_framework.conversational_ingestion.FormCompletionStrategy;
import com.kairos.agentic_framework.conversational_ingestion.form.Form;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrainingOpportunityCompletionStrategy implements FormCompletionStrategy {

    private final TrainingOpportunityService opportunityService;

    @Override
    public String getEntityName() {
        return "Training Opportunity"; // Must match @ConversationalEntity name
    }

    @Override
    public void execute(Form completedForm) {
        log.info("Executing completion strategy for a new Training Opportunity form.");

        // Extract and cast the values from the completed form.
        String title = (String) completedForm.getFieldValueAsObject("title");
        String providerName = (String) completedForm.getFieldValueAsObject("providerName");
        String skillCategory = (String) completedForm.getFieldValueAsObject("skillCategory");
        int durationWeeks = Integer.parseInt((String) completedForm.getFieldValueAsObject("durationWeeks"));
        double cost = Double.parseDouble((String) completedForm.getFieldValueAsObject("cost"));
        String location = (String) completedForm.getFieldValueAsObject("location");
        
        // Delegate the actual creation logic to the service layer.
        opportunityService.createOpportunity(title, providerName, skillCategory, durationWeeks, cost, location);
    }
}