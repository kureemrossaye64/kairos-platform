package com.kairos.sports_atlas.facility.service;

import com.kairos.agentic.conversational.ExampleProviderStrategy;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.repositories.FacilityRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FacilityExampleProvider implements ExampleProviderStrategy {
    private final FacilityRepository facilityRepository;

    @Override
    public List<String> getExamples() {
        return facilityRepository.findAll().stream()
                .map(Facility::getName)
                .limit(5) // Limit to a few examples
                .collect(Collectors.toList());
    }
}