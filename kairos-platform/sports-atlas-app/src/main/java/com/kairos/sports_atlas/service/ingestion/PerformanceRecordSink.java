package com.kairos.sports_atlas.service.ingestion;

import com.kairos.ingestion.pipeline.Sink;
import com.kairos.sports_atlas.entities.Athlete;
import com.kairos.sports_atlas.entities.PerformanceRecord;
import com.kairos.sports_atlas.facility.dto.PerformanceDataDto;
import com.kairos.sports_atlas.repositories.AthleteRepository;
import com.kairos.sports_atlas.repositories.PerformanceRecordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

/**
 * The Sink for performance data. It consumes PerformanceDataDto objects,
 * finds or creates the corresponding Athlete, and saves the new PerformanceRecord.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceRecordSink implements Sink<PerformanceDataDto> {

    private final AthleteRepository athleteRepository;
    private final PerformanceRecordRepository performanceRecordRepository;

    @Override
    @Transactional // Wrap the entire consumption in a single transaction for efficiency
    public void consume(Stream<PerformanceDataDto> stream) {
        stream.forEach(dto -> {
            try {
                // 1. Find or create the athlete to avoid duplicates.
                Athlete athlete = athleteRepository.findByNameAndDateOfBirth(dto.getAthleteName(), dto.getDateOfBirth())
                        .orElseGet(() -> {
                            log.info("Creating new athlete: {}", dto.getAthleteName());
                            Athlete newAthlete = new Athlete(dto.getAthleteName(), dto.getDateOfBirth(), dto.getSport());
                            return athleteRepository.save(newAthlete);
                        });

                // 2. Create and save the new performance record
                PerformanceRecord record = new PerformanceRecord();
                record.setAthlete(athlete);
                record.setEventName(dto.getEventName());
                record.setResult(dto.getResult());
                record.setUnit(dto.getUnit());
                record.setEventDate(dto.getEventDate());
                
                performanceRecordRepository.save(record);
                log.debug("Saved performance record for {}", athlete.getName());

            } catch (Exception e) {
                log.error("Failed to process and save performance DTO for athlete: {}", dto.getAthleteName(), e);
                // Continue with the next item in the stream
            }
        });
    }
}