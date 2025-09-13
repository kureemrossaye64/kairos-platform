package com.kairos.sports_atlas.services;

import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.Athlete;
import com.kairos.sports_atlas.repositories.AthleteRepository;

@Service
public class AthleteService extends GenericCrudService<Athlete, AthleteRepository>{

	protected AthleteService(AthleteRepository repository) {
		super(repository);
	}

}
