package com.kairos.ingestion.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kairos.core.ingestion.SourceRecord;
import com.kairos.core.ingestion.SourceRecordService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemorySourceRecordService implements SourceRecordService {

    private final Map<UUID, SourceRecord> store = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File persistenceFile = new File("./data/source-records.json");

    @Override
    public SourceRecord save(SourceRecord record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
            record.setCreatedAt(Instant.now());
        }
        record.setUpdatedAt(Instant.now());
        store.put(record.getId(), record);
        return record;
    }

    @Override
    public Optional<SourceRecord> retrieve(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    // --- Persistence Logic ---

    @PostConstruct
    public void loadFromFile() {
        if (persistenceFile.exists()) {
            try {
                log.info("Loading SourceRecords from {}", persistenceFile.getAbsolutePath());
                Map<UUID, SourceRecord> data = mapper.readValue(persistenceFile, new TypeReference<Map<UUID, SourceRecord>>() {});
                store.putAll(data);
            } catch (IOException e) {
                log.warn("Could not load source records from file", e);
            }
        }
    }

    @PreDestroy
    public void saveToFile() {
        try {
            if (!persistenceFile.getParentFile().exists()) {
                persistenceFile.getParentFile().mkdirs();
            }
            mapper.writeValue(persistenceFile, store);
            log.info("Saved {} SourceRecords to disk.", store.size());
        } catch (IOException e) {
            log.error("Failed to persist SourceRecords", e);
        }
    }
}