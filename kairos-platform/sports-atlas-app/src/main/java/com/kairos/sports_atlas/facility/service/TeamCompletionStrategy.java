package com.kairos.sports_atlas.facility.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.kairos.agentic.transactional.TransactionCompletionStrategy;
import com.kairos.agentic.transactional.TransactionContext;
import com.kairos.notification.NotificationService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.Team;
import com.kairos.sports_atlas.entities.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamCompletionStrategy implements TransactionCompletionStrategy {
    
    private final TeamService teamService; // A new service for team logic
    private final NotificationService notificationService;

    @Override
    public String getTransactionName() {
        return "Team";
    }

    @Override
    @SuppressWarnings("unchecked") // We are confident in the types from the processors
    public String execute(TransactionContext context) {
        String name = (String) context.getFields().get("name").getValue();
        Activity activity = (Activity) context.getFields().get("activity").getValue();
        User captain = (User) context.getFields().get("captain").getValue();
        Set<User> members = (Set<User>) context.getFields().get("members").getValue();
        
        // Add the captain to the member list if they aren't already there
        members.add(captain);
        
        Team newTeam = teamService.createTeam(name, activity, captain, members);

        // Send notifications to all members except the captain
        String subject = "You've been invited to join " + newTeam.getName() + "!";
        String body = String.format("Hi there! You've been invited by captain %s to join the new %s team, '%s'. Log in to the platform to accept.",
                captain.getUsername(), activity.getName(), newTeam.getName());
        
        newTeam.getMembers().stream()
               .filter(member -> !member.getId().equals(captain.getId()))
               .forEach(member -> notificationService.send(member.getUsername(), subject, body));

        return "Success! The team '" + newTeam.getName() + "' has been created and invitations have been sent to the members.";
    }
}