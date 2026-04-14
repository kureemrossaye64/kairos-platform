package com.kairos.pulsberry.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.pulsberry.entity.JpaSourceRecord;

public interface SourceRecordRepository extends JpaRepository<JpaSourceRecord, UUID> {
}