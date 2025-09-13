package com.kairos.sports_atlas.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.kairos.agentic_framework.tools.KairosTool;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.facility.service.FacilityService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@KairosTool
@RequiredArgsConstructor
@Slf4j
public class FacilityTool {

	private final FacilityService facilityService;

	private static final double BASE_RADIUS_KM = 5.0;
    private static final double EXPANDED_RADIUS_KM = 15.0;

    @Tool("Searches for an available sports facility and suggests alternatives if the initial request is unavailable. Handles booking searches. You should make necessary guess for parameters that the user has not provided")
    public String findAndSuggestFacilities(
            @P("The sport or activity, e.g., 'Football', 'Swimming'.") String activityName,
            @P("The central location to search from, e.g., 'Curepipe'.") String location,
            @P("Optional The desired date and start time in 'YYYY-MM-DD HH:mm' format. If the user did not provide any information about the desired date and time, please make assumptions") String startDateTime,
            @P("Optional The duration of the booking in hours, e.g., 1.5 for 90 minutes. If the user did not provide duration, please make educated assumptions") double durationHours,
            @P("Optional search radius in kilometers") Double radiusInKm
    ) {
        log.info("Agent tool invoked: findAndSuggestFacilities for {} in {} at {}", activityName, location, startDateTime);
        LocalDateTime startTime = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime endTime = startTime.plusMinutes((long) (durationHours * 60));
        double radius = (radiusInKm == null) ? BASE_RADIUS_KM : radiusInKm;
        
        List<String> suggestions = new ArrayList<>();

        // 1. Primary Search: The user's exact request
        List<Facility> exactMatches = facilityService.findAvailableNearby(location, activityName, radius, startTime, endTime);
        if (!exactMatches.isEmpty()) {
            return formatResults("Here are the available facilities matching your request:", exactMatches);
        }

        // 2. No exact matches found, start finding alternatives
        log.info("No exact match found. Searching for alternatives...");

        // Alt 1: Expand search radius
        List<Facility> fartherMatches = facilityService.findAvailableNearby(location, activityName, radius*3, startTime, endTime);
        fartherMatches.removeAll(exactMatches); // Should be empty, but good practice
        if (!fartherMatches.isEmpty()) {
            suggestions.add(formatResults("ALTERNATIVE - Farther location (up to 15km):", fartherMatches));
        }

        // Alt 2: Suggest next day, same time
        LocalDateTime nextDayStartTime = startTime.plusDays(1);
        LocalDateTime nextDayEndTime = endTime.plusDays(1);
        List<Facility> nextDayMatches = facilityService.findAvailableNearby(location, activityName, BASE_RADIUS_KM, nextDayStartTime, nextDayEndTime);
        if (!nextDayMatches.isEmpty()) {
            suggestions.add(formatResults(String.format("ALTERNATIVE - Same time tomorrow (%s):", nextDayStartTime.toLocalDate()), nextDayMatches));
        }

        // Alt 3: Suggest later same day
        LocalDateTime laterStartTime = startTime.plusHours(2);
        LocalDateTime laterEndTime = endTime.plusHours(2);
        List<Facility> laterMatches = facilityService.findAvailableNearby(location, activityName, BASE_RADIUS_KM, laterStartTime, laterEndTime);
        if (!laterMatches.isEmpty()) {
            suggestions.add(formatResults(String.format("ALTERNATIVE - Later today (starting at %s):", laterStartTime.toLocalTime()), laterMatches));
        }

        if (suggestions.isEmpty()) {
            return "I'm sorry, I couldn't find any available slots for " + activityName + " near " + location + " matching your request or any nearby alternatives. Please try a different day or location.";
        }

        return "The time you requested is unavailable. However, I found some alternative options for you:\n\n" + String.join("\n\n", suggestions);
    }

    private String formatResults(String title, List<Facility> facilities) {
        String facilityList = facilities.stream()
                .map(f -> String.format("- %s in %s", f.getName(), f.getLocation()))
                .collect(Collectors.joining("\n"));
        return title + "\n" + facilityList;
    }
}