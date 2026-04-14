package com.kairos.ingestion.persistence;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ingestion.ProcessingStatus;
import com.kairos.core.ingestion.SourceRecord;
import com.kairos.core.ingestion.SourceRecordService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcSourceRecordService implements SourceRecordService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    private final RowMapper<SourceRecord> rowMapper;

    @SuppressWarnings("unchecked")
	public JdbcSourceRecordService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		super();
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
		
		rowMapper = (rs, rowNum) -> {
	        SourceRecord r = new SourceRecord();
	        r.setId(UUID.fromString(rs.getString("id")));
	        r.setStorageUri(rs.getString("storage_uri"));
	        r.setSourceName(rs.getString("source_name"));
	        r.setContentType(rs.getString("content_type"));
	        r.setStatus(ProcessingStatus.valueOf(rs.getString("status")));
	        r.setFailureReason(rs.getString("failure_reason"));
	        r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
	        r.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
	        
	        try {
	            String json = rs.getString("metadata_json");
	            if (json != null) {
	                r.setMetadataManifest(objectMapper.readValue(json, Map.class));
	            }
	        } catch (IOException e) {
	            log.error("Failed to parse metadata JSON", e);
	        }
	        return r;
	    };
	}

	@PostConstruct
    public void initTable() {
        // Simple DDL check. In production, use Flyway/Liquibase, but this makes the starter standalone.
        log.info("Initializing JDBC Table: source_records");
        String sql = """
            CREATE TABLE IF NOT EXISTS source_records (
                id UUID PRIMARY KEY,
                storage_uri VARCHAR(1024),
                source_name VARCHAR(255),
                content_type VARCHAR(255),
                status VARCHAR(50),
                failure_reason TEXT,
                metadata_json TEXT,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
        """;
        jdbcTemplate.execute(sql);
    }

    @Override
    public SourceRecord save(SourceRecord record) {
        if (record.getId() == null) {
            return insert(record);
        } else {
            return update(record);
        }
    }

    private SourceRecord insert(SourceRecord record) {
        record.setId(UUID.randomUUID());
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());

        String sql = """
            INSERT INTO source_records 
            (id, storage_uri, source_name, content_type, status, failure_reason, metadata_json, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                record.getId(),
                record.getStorageUri(),
                record.getSourceName(),
                record.getContentType(),
                record.getStatus().name(),
                record.getFailureReason(),
                toJson(record.getMetadataManifest()),
                Timestamp.from(record.getCreatedAt()),
                Timestamp.from(record.getUpdatedAt())
        );
        return record;
    }

    private SourceRecord update(SourceRecord record) {
        record.setUpdatedAt(Instant.now());
        String sql = """
            UPDATE source_records SET 
                storage_uri = ?, 
                source_name = ?, 
                content_type = ?, 
                status = ?, 
                failure_reason = ?, 
                metadata_json = ?, 
                updated_at = ? 
            WHERE id = ?
        """;

        int updated = jdbcTemplate.update(sql,
                record.getStorageUri(),
                record.getSourceName(),
                record.getContentType(),
                record.getStatus().name(),
                record.getFailureReason(),
                toJson(record.getMetadataManifest()),
                Timestamp.from(record.getUpdatedAt()),
                record.getId()
        );

        if (updated == 0) {
            // Fallback if ID was set but not found in DB
            return insert(record);
        }
        return record;
    }

    @Override
    public Optional<SourceRecord> retrieve(UUID id) {
        String sql = "SELECT * FROM source_records WHERE id = ?";
        try {
            SourceRecord record = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(record);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    

    private String toJson(Map<String, Object> map) {
        try {
            return map == null ? "{}" : objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}