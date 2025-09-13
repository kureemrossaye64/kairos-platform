package com.kairos.sports_atlas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.Team;

public interface TeamRepository extends JpaRepository<Team, UUID>{

}
