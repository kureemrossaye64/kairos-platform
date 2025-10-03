package com.kairos.sports_atlas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.JpaSourceRecord;

public interface SourceRecordRepository extends JpaRepository<JpaSourceRecord, UUID> {
}