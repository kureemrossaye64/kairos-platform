package com.kairos.sports_atlas.repositories;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kairos.sports_atlas.entities.PlayerLfg;

public interface PlayerLfgRepository extends JpaRepository<PlayerLfg, UUID> {
	@Query(value = "SELECT p.* FROM player_lfg p " + "WHERE p.activity_id = :activityId "
			+ "AND ST_DWithin(p.location, :locationPoint, :radiusInMeters) "
			+ "AND p.created_at >= NOW() - INTERVAL '7 days'", nativeQuery = true)
	List<PlayerLfg> findRecentLfgPlayers(@Param("activityId") UUID activityId,
			@Param("locationPoint") Point locationPoint, @Param("radiusInMeters") double radiusInMeters);
}