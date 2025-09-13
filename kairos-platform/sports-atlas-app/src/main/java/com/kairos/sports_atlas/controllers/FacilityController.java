package com.kairos.sports_atlas.controllers;

import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.facility.dto.FacilityDto;
import com.kairos.sports_atlas.facility.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * Endpoint to get a list of all facilities in the system.
     * @return A list of Facility objects.
     */
    @GetMapping
    public ResponseEntity<List<FacilityDto>> getAllFacilities() {
        List<Facility> facilities = facilityService.findAll();
        return ResponseEntity.ok(facilities.stream().map(FacilityDto::fromEntity).collect(Collectors.toList()));
    }
}