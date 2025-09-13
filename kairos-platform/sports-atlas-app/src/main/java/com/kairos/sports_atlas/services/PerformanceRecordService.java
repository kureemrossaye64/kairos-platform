package com.kairos.sports_atlas.services;

import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.PerformanceRecord;
import com.kairos.sports_atlas.repositories.PerformanceRecordRepository;

@Service
public class PerformanceRecordService extends GenericCrudService<PerformanceRecord, PerformanceRecordRepository>{

	protected PerformanceRecordService(PerformanceRecordRepository repository) {
		super(repository);
	}

}
