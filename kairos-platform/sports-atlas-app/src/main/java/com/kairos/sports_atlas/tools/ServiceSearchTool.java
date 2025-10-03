package com.kairos.sports_atlas.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.kairos.agentic.tools.KairosTool;
import com.kairos.core.search.SearchQuery;
import com.kairos.sports_atlas.common.GeocodingService;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.facility.service.ServiceEntityService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;

@KairosTool
@RequiredArgsConstructor
public class ServiceSearchTool {

    private final ServiceEntityService serviceEntityService;
    private final GeocodingService geocodingService;

    @Tool("Searches the national registry for ANY type of public or private service, facility, course, or opportunity available to the community. Use this for all user queries related to finding something.")
    public List<ServiceEntity> findServices(
            @P("A natural language description of what the user is looking for. This should be as detailed as possible, e.g., 'a place to play football', 'a coding course', 'a Jiu-Jitsu dojo'.") String query,
            @P("Optional: A specific town, landmark, or area to search near. e.g., 'Curepipe', 'Bagatelle Mall'.") String location
    ) {
        SearchQuery.SearchQueryBuilder queryBuilder = SearchQuery.builder().textQuery(query);

        if (location != null && !location.isBlank()) {
            try {
                queryBuilder.location(geocodingService.geocode(location));
                queryBuilder.radiusInKm(5.0); // Default search radius of 10km
            } catch (Exception e) {
                // If geocoding fails, we can still perform a text search including the location name
                // This makes the tool more robust.
            }
        }

        List<ServiceEntity> services = serviceEntityService.findServices(queryBuilder.build());
        return services;
		/*
		 * if (services.isEmpty()) { return
		 * "I'm sorry, I couldn't find any services, facilities, or courses matching your request."
		 * ; }
		 * 
		 * String formattedResults = services.stream() .map(service -> String.format(
		 * "- %s (%s) in %s. Description: %s", service.getName(),
		 * service.getServiceType(), // e.g., "Public Sports Facility"
		 * service.getLocation(), service.getDescription() )) .limit(5) // Limit the
		 * number of results to keep the chat response concise
		 * .collect(Collectors.joining("\n"));
		 * 
		 * return "I found the following matching services for you:\n" +
		 * formattedResults;
		 */
    }
}