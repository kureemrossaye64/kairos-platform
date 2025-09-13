package com.kairos.sports_atlas.facility.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.kairos.agentic_framework.agent.Context;
import com.kairos.agentic_framework.agent.ContextAwareStrategy;
import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.sports_atlas.repositories.ServiceEntityRepository;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class StartTimeFieldProcessor implements FieldProcessorStrategy , ContextAwareStrategy{
	
	

    private final ServiceEntityRepository facilityRepository;

    private Context context;
    @Override
    public ProcessingResult process(String rawInput) {
        // This processor is more complex; it depends on other fields being filled first.
        // A full implementation would require access to the TransactionContext.
        // For this POC, we will simplify and only validate the format.
        // The final availability check will happen in the CompletionStrategy.
        try {
            LocalDateTime startTime = LocalDateTime.parse(rawInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            if (startTime.isBefore(LocalDateTime.now())) {
                return ProcessingResult.failure("The booking time must be in the future.");
            }
            return ProcessingResult.success(startTime);
        } catch (DateTimeParseException e) {
            return ProcessingResult.failure("That doesn't look like a valid date and time. Please use the format 'YYYY-MM-DD HH:mm', for example: '2025-10-27 15:00'.");
        }
    }

	@Override
	public void setContext(Context ctx) {
		this.context = ctx;
		
	}
}