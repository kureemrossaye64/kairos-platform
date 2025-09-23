package com.kairos.sports_atlas.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.kairos.agentic_framework.tools.KairosTool;
import com.kairos.search.model.SearchQuery;
import com.kairos.search.model.SearchResult;
import com.kairos.sports_atlas.entities.TrainingOpportunity;
import com.kairos.sports_atlas.repositories.TrainingOpportunityRepository;
import com.kairos.vector_search.service.VectorStoreService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;

//@KairosTool
@RequiredArgsConstructor
public class OpportunityTool {

    private final VectorStoreService searchService;
    private final TrainingOpportunityRepository opportunityRepository; // For hydrating results

    @Tool("Searches for training opportunities, courses, or bootcamps based on a skill and optional location.")
    
    public String findTrainingOpportunities(
            @P("The skill or category the user is interested in, e.g., 'coding', 'design', 'hospitality'") String skill,
            @P("Optional: The location to search near, e.g., 'Port Louis'") String location
    ) {
		/*
		 * SearchQuery queryBuilder = new SearchQuery.builder().textQuery(skill);
		 * 
		 * // The AI might not provide a location, so this is optional. if (location !=
		 * null && !location.isBlank()) { // We need GeocodingService here if we want to
		 * search by proximity // For simplicity in this tool, we'll just add it to the
		 * text query. // A more advanced version would use the geo capabilities. }
		 * 
		 * SearchResult result = searchService.search( queryBuilder.build(),
		 * TrainingOpportunity.class);
		 * 
		 * if (result.totalHits() == 0) { return
		 * "I'm sorry, I couldn't find any training opportunities matching '" + skill +
		 * "'."; }
		 * 
		 * List<TrainingOpportunity> opportunities =
		 * opportunityRepository.findAllById(result.documentIds());
		 * 
		 * String formattedResults = opportunities.stream() .map(opp -> String.format(
		 * "- '%s' by %s (%s). Duration: %d weeks, Cost: Rs %.2f. Location: %s.",
		 * opp.getTitle(), opp.getProviderName(), opp.getSkillCategory(),
		 * opp.getDurationWeeks(), opp.getCost(), opp.getLocation() ))
		 * .collect(Collectors.joining("\n"));
		 * 
		 * return "I found the following training opportunities for you:\n" +
		 * formattedResults;
		 */    
    	return "";
    }
}