package com.kairos.sports_atlas.partner.service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.common.GeocodingService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.Partner;
import com.kairos.sports_atlas.repositories.ActivityRepository;
import com.kairos.sports_atlas.repositories.PartnerRepository;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PartnerService extends GenericCrudService<Partner, PartnerRepository> {

    private final GeocodingService geocodingService;
    private final ActivityRepository activityRepository;

    public PartnerService(PartnerRepository repository,
                          GeocodingService geocodingService,
                          ActivityRepository activityRepository) {
        super(repository);
        this.geocodingService = geocodingService;
        this.activityRepository = activityRepository;
    }

    /**
     * A specialized method to create a new Partner from raw data.
     * This method contains the core business logic for creating a partner.
     *
     * @param name The partner's name.
     * @param activityName The name of the activity they offer.
     * @param location The address or location string.
     * @param contactInfo Their contact details.
     * @param description A description of their services.
     * @return The newly created and persisted Partner entity.
     */
    @Transactional
    public Partner createPartner(String name, String activityName, String location, String contactEmail, String contactPhone, String description) {
        log.info("Creating new partner: {}", name);

        // 1. Find or create the associated Activity.
        Activity activity = activityRepository.findByName(activityName)
                .orElseGet(() -> {
                    log.warn("Activity '{}' not found. Creating a new one for partner '{}'.", activityName, name);
                    return activityRepository.save(new Activity(activityName));
                });

        // 2. Geocode the location.
        Point coordinates = geocodingService.geocode(location);

        // 3. Build the new Partner entity.
        Partner partner = new Partner();
        partner.setName(name);
        partner.setActivity(activity);
        partner.setLocation(location);
        partner.setContactEmail(contactEmail);
        partner.setContactPhone(contactPhone);
        partner.setDescription(description);
        partner.setCoordinates(coordinates);

        // 4. Save the partner using the inherited 'save' method.
        return this.save(partner);
    }
}