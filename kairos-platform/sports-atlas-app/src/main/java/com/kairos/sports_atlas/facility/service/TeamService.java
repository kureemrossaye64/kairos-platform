package com.kairos.sports_atlas.facility.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.Team;
import com.kairos.sports_atlas.entities.User;
import com.kairos.sports_atlas.repositories.TeamRepository;

@Service
public class TeamService extends GenericCrudService<Team, TeamRepository>{

	protected TeamService(TeamRepository repository) {
		super(repository);
	}

	public Team createTeam(String name, Activity activity, User captain, Set<User> members) {
		Team team = new Team();
		team.setName(name);
		team.setActivity(activity);
		team.setCaptain(captain);
		team.setMembers(members);
		save(team);
		return team;
	}

}
