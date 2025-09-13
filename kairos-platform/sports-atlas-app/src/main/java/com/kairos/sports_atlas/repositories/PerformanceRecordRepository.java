package com.kairos.sports_atlas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.PerformanceRecord;

import java.util.List;
import java.util.UUID;

public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, UUID> {
    List<PerformanceRecord> findByAthleteIdOrderByEventDateDesc(UUID athleteId);
    List<PerformanceRecord> findByEventNameOrderByResultAsc(String eventName);
}