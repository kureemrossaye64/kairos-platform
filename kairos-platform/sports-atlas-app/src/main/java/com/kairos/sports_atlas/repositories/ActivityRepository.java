package com.kairos.sports_atlas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.Activity;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    Optional<Activity> findByName(String name);
}