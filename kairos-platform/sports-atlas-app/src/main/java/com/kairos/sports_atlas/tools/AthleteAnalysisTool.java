package com.kairos.sports_atlas.tools;

import com.kairos.agentic.tools.KairosTool;
import com.kairos.sports_atlas.entities.Athlete;
import com.kairos.sports_atlas.entities.PerformanceRecord;
import com.kairos.sports_atlas.repositories.AthleteRepository;
import com.kairos.sports_atlas.repositories.PerformanceRecordRepository;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@KairosTool
@RequiredArgsConstructor
@Slf4j
@Service
public class AthleteAnalysisTool {

    private final AthleteRepository athleteRepository;
    private final PerformanceRecordRepository performanceRecordRepository;

    @Tool("Retrieves the performance history for a specific athlete by their full name.")
    public String getSpecificAthletePerformanceSummary(String athleteName) {
        log.info("AI Agent is searching for athlete: '{}'", athleteName);
        // This is a simplification; a real system would handle multiple athletes with the same name.
        Optional<Athlete> athleteOpt = athleteRepository.findByNameAndDateOfBirth(athleteName, null); // Simplified find
        
        if (athleteOpt.isEmpty()) {
            return "Could not find any athlete named " + athleteName;
        }

        List<PerformanceRecord> records = performanceRecordRepository.findByAthleteIdOrderByEventDateDesc(athleteOpt.get().getId());

        if (records.isEmpty()) {
            return athleteName + " has no performance records in the system.";
        }
        
        String summary = records.stream()
            .map(r -> String.format("On %s, in the event '%s', they achieved a result of %s %s.",
                r.getEventDate(), r.getEventName(), r.getResult(), r.getUnit()))
            .collect(Collectors.joining("\n"));
            
        return "Performance summary for " + athleteName + ":\n" + summary;
    }
    
    @Tool("Retrieves the performance history and information about athletes")
    public String getAthletePerformanceSummary() {
        log.info("AI Agent is searching for athlete:");
        // This is a simplification; a real system would handle multiple athletes with the same name.
        List<PerformanceRecord> records = performanceRecordRepository.findAll();
        
        
        String summary = records.stream()
            .map(r -> String.format("Athlete %s, On %s, in the event '%s', they achieved a result of %s %s.",r.getAthlete().getName(),
                r.getEventDate(), r.getEventName(), r.getResult(), r.getUnit()))
            .collect(Collectors.joining("\n"));
            
        return "Performance:\n" + summary;
    }
}