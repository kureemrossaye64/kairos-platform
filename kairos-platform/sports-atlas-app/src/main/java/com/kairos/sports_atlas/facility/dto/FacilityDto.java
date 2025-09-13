package com.kairos.sports_atlas.facility.dto;

import com.kairos.sports_atlas.entities.Facility;

import lombok.Data;

@Data
public class FacilityDto {

    private String name;

    private String type; // e.g., "Football Pitch", "Swimming Pool", "Tennis Court"

    private String location; // e.g., "Curepipe", "Port Louis"

    private int capacity;
    
    

    public static FacilityDto fromEntity(Facility f) {
    	FacilityDto d = new FacilityDto();
    	d.setCapacity(f.getCapacity());
    	d.setLocation(f.getLocation());
    	d.setName(f.getName());
    	d.setType(f.getType());
    	return d;
    }
}
