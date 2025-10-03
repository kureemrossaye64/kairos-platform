package com.kairos.core.ingestion;

import java.util.Optional;
import java.util.UUID;

public interface SourceRecordService {

	SourceRecord save(SourceRecord record);

	Optional<SourceRecord> retrieve(UUID sourceRecordId);

}
