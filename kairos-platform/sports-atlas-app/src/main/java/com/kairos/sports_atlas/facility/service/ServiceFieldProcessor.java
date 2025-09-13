package com.kairos.sports_atlas.facility.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kairos.agentic_framework.agent.Context;
import com.kairos.agentic_framework.agent.ContextAwareStrategy;
import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.search.model.SearchQuery;
import com.kairos.search.model.SearchResult;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.repositories.FacilityRepository;
import com.kairos.sports_atlas.repositories.ServiceEntityRepository;
import com.kairos.vector_search.service.VectorStoreService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceFieldProcessor implements FieldProcessorStrategy, ContextAwareStrategy {
    private final VectorStoreService searchService;
	
	private final ServiceEntityRepository serviceRepository;
	
	private Context context;

    @Override
    public ProcessingResult process(String rawInput) {
        // 1. Try for an exact match first (case-insensitive)
    	
    	SearchResult result = searchService.search(SearchQuery.builder().textQuery(rawInput).build(), ServiceEntity.class);
    	
    	List<ServiceEntity> candidates = serviceRepository.findAllById(result.documentIds());
    	
    	 Optional<ServiceEntity> exactMatch = candidates.stream()
                 .filter(f -> f.getName().equalsIgnoreCase(rawInput))
                 .findFirst();

         if (exactMatch.isPresent()) {
             if (!exactMatch.get().isBookable()) { // New check!
                 return ProcessingResult.failure("The service '" + exactMatch.get().getName() + "' is not a bookable service.");
             }
             return ProcessingResult.success(exactMatch.get());
         }
    	
         if (!candidates.isEmpty()) {
             String suggestions = candidates.stream().map(ServiceEntity::getName).limit(3).collect(Collectors.joining(", "));
             String guidance = String.format("I couldn't find a service named exactly '%s'. Did you mean one of these: %s?", rawInput, suggestions);
             return ProcessingResult.failure(guidance);
         }
         
         return ProcessingResult.failure("I'm sorry, I couldn't find any facility matching '" + rawInput + "'.");
    	
		
    }

	@Override
	public void setContext(Context ctx) {
		this.context = ctx;
		
	}
}