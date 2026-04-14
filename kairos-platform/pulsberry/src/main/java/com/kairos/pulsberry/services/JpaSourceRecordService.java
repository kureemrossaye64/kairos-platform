package com.kairos.pulsberry.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.ingestion.SourceRecord;
import com.kairos.core.ingestion.SourceRecordService;
import com.kairos.pulsberry.entity.JpaSourceRecord;
import com.kairos.pulsberry.repositories.SourceRecordRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JpaSourceRecordService implements SourceRecordService{
	
	private final SourceRecordRepository repository;

	@Override
	@Transactional
	public SourceRecord save(SourceRecord i) {
		UUID id = i.getId();
		if(id == null) {
			SourceRecord result =  repository.saveAndFlush(JpaSourceRecord.from(i)).toDto();
			return result;
		}else {
			JpaSourceRecord o = repository.findById(id).orElseThrow(()->  new IllegalStateException("cannot find record"));
			o.setContentType(i.getContentType());
	    	o.setCreatedAt(i.getCreatedAt());
	    	o.setFailureReason(i.getFailureReason());
	    	o.setId(i.getId());
	    	o.setMetadataManifest(i.getMetadataManifest());
	    	o.setSourceName(i.getSourceName());
	    	o.setStatus(i.getStatus());
	    	o.setStorageUri(i.getStorageUri());
	    	o.setUpdatedAt(i.getUpdatedAt());
	    	return repository.saveAndFlush(o).toDto();
		}
	}

	@Override
	@Transactional
	public Optional<SourceRecord> retrieve(UUID sourceRecordId) {
		JpaSourceRecord o = repository.findById(sourceRecordId).orElse(null);
		if(o != null) {
			return Optional.of(o.toDto());
		}else {
			return Optional.empty();
		}
	}

}
