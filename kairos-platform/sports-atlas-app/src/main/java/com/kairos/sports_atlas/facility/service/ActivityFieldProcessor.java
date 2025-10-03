package com.kairos.sports_atlas.facility.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kairos.agentic.conversational.FieldProcessorStrategy;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.repositories.ActivityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityFieldProcessor implements FieldProcessorStrategy {
    private final ActivityRepository activityRepository;

    @Override
    public ProcessingResult process(String rawInput) {
        
    	Optional<Activity> candidate = activityRepository.findByName(rawInput.trim());
    	
    	if(candidate.isEmpty()) {
    		
    		List<Activity> activities = activityRepository.findAll();
    		
    		String acts = activities.stream().map(Activity::getName).collect(Collectors.joining(","));
    		
    		return ProcessingResult.failure("I don't recognize the activity '" + rawInput + "'. Please choose from available activities:" + acts);
    		
    	}else {
    		return ProcessingResult.success(candidate.get());
    	}
    }
}