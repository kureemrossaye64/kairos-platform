package com.kairos.sports_atlas.facility.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFieldProcessor implements FieldProcessorStrategy {
    private final UserRepository userRepository;

    @Override
    public ProcessingResult process(String rawInput) {
        

          Optional<User> user =  userRepository.findByUsername(rawInput);
          
          if(user.isEmpty()) {
        	  String guidance = "I cannot find the user " + rawInput + " Please correct the username then proceed";
        	  return ProcessingResult.failure(guidance);
          }

        

        return ProcessingResult.success(user.get()); // Return the set of User objects
    }
}