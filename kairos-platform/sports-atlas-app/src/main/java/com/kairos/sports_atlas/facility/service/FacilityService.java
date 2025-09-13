package com.kairos.sports_atlas.facility.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.common.GeocodingService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.repositories.FacilityRepository;


@Service
public class FacilityService extends GenericCrudService<Facility, FacilityRepository> {
	
	private final GeocodingService geocodingService;
	
    public FacilityService(FacilityRepository repository, GeocodingService geocodingService) {
        super(repository);
        this.geocodingService = geocodingService;
    }
    
    
    @Deprecated
    public List<Facility> findAvailableNearby(String locationQuery, String activityName, double radiusInKm, LocalDateTime start, LocalDateTime end) {
       // Point locationPoint = geocodingService.geocode(locationQuery);
        return new ArrayList<Facility>();
        //return repository.findAvailableNearbyFacilitiesByActivity(locationPoint, activityName, radiusInKm * 1000, start, end);
    }


    public Facility createFacility(String name, String type, String location, int capacity, Set<Activity> activities) {
		Facility f = new Facility();
		f.setName(name);
		f.setType(type);
		f.setLocation(location);
		f.setCapacity(capacity);
		f.setSupportedActivities(activities);
		Point coordinates = geocodingService.geocode(location);
		f.setCoordinates(coordinates);
		save(f);
		return f;
	}
   
}