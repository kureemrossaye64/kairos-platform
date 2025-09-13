package com.kairos.sports_atlas.training.service;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.common.GeocodingService;
import com.kairos.sports_atlas.entities.TrainingOpportunity;
import com.kairos.sports_atlas.repositories.TrainingOpportunityRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrainingOpportunityService extends GenericCrudService<TrainingOpportunity, TrainingOpportunityRepository> {

    private final GeocodingService geocodingService;

    public TrainingOpportunityService(TrainingOpportunityRepository repository, GeocodingService geocodingService) {
        super(repository);
        this.geocodingService = geocodingService;
    }

    @Transactional
    public TrainingOpportunity createOpportunity(String title, String providerName, String skillCategory,
                                                 int durationWeeks, double cost, String location) {
        log.info("Creating new training opportunity: {}", title);
        
        Point coordinates = geocodingService.geocode(location);

        TrainingOpportunity opportunity = new TrainingOpportunity();
        opportunity.setTitle(title);
        opportunity.setProviderName(providerName);
        opportunity.setSkillCategory(skillCategory);
        opportunity.setDurationWeeks(durationWeeks);
        opportunity.setCost(cost);
        opportunity.setLocation(location);
        opportunity.setCoordinates(coordinates);

        // The save method will automatically publish an EntityChangeEvent
        // which our generic listener will pick up for indexing.
        return this.save(opportunity);
    }
}