package com.kairos.sports_atlas.tools;

import java.util.List;
import java.util.stream.Collectors;

import com.kairos.agentic.tools.KairosTool;
import com.kairos.sports_atlas.entities.PlayerLfg;
import com.kairos.sports_atlas.lfg.service.LfgService;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;

@KairosTool
@RequiredArgsConstructor
public class LfgTool {
    private final LfgService lfgService;

    @Tool("Finds other players who are also looking for a game of a specific activity near a location.")
    public String findPlayersLookingForGame(String activityName, String location) {
        List<PlayerLfg> players = lfgService.findPlayers(activityName, location);
        if (players.isEmpty()) {
            return "No players are currently looking for a game of " + activityName + " near " + location + ".";
        }
        return "Found " + players.size() + " player(s) looking for a game. Their names are: " + 
               players.stream().map(p -> p.getUser().getUsername()).collect(Collectors.joining(", "));
    }

    @Tool("Adds the current user to a list of players looking for a game of a specific activity near a location.")
    public String addPlayerToLookingForGame(String activityName, String location) {
        // In a real app, we'd get the user from SecurityContextHolder.
        String currentUsername = "poc-user"; // Hardcoded for demo
        lfgService.addPlayer(activityName, location, currentUsername);
        return "You have been successfully added to the list of players looking for a " + activityName + " game near " + location + ". We will notify you if a group forms.";
    }
}