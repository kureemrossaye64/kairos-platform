package com.kairos.sports_atlas.facility.service;

import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.repositories.ActivityRepository;

@Service
public class ActivityService extends GenericCrudService<Activity, ActivityRepository>{

	protected ActivityService(ActivityRepository repository) {
		super(repository);
	}

}
