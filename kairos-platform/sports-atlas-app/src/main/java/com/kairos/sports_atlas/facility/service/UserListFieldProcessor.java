package com.kairos.sports_atlas.facility.service;

import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserListFieldProcessor implements FieldProcessorStrategy {
    private final UserRepository userRepository;

    @Override
    public ProcessingResult process(String rawInput) {
        List<String> usernames = Arrays.stream(rawInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (usernames.isEmpty()) {
            return ProcessingResult.failure("Please provide at least one username.");
        }

        Set<User> foundUsers = new HashSet<>();
        Set<String> notFoundUsernames = new HashSet<>();

        for (String username : usernames) {
            userRepository.findByUsername(username)
                .ifPresentOrElse(foundUsers::add, () -> notFoundUsernames.add(username));
        }

        if (!notFoundUsernames.isEmpty()) {
            String guidance = "I found some users, but could not find registered users for: " + String.join(", ", notFoundUsernames)
                    + ". Please correct their usernames or remove them to proceed.";
            return ProcessingResult.failure(guidance);
        }

        return ProcessingResult.success(new HashSet<>(foundUsers)); // Return the set of User objects
    }
}