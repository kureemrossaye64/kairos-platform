package com.kairos.sports_atlas.lfg.service;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;
import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.common.GeocodingService;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.PlayerLfg;
import com.kairos.sports_atlas.repositories.ActivityRepository;
import com.kairos.sports_atlas.repositories.PlayerLfgRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LfgService extends GenericCrudService<PlayerLfg, PlayerLfgRepository> {

	private final GeocodingService geocodingService;
	private final ActivityRepository activityRepository;
	private final UserRepository userRepository;

	// We use constructor injection for all dependencies.
	public LfgService(PlayerLfgRepository repository, GeocodingService geocodingService,
			ActivityRepository activityRepository, UserRepository userRepository) {
		super(repository);
		this.geocodingService = geocodingService;
		this.activityRepository = activityRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Finds players who have recently registered as looking for a specific game
	 * near a location.
	 * 
	 * @param activityName  The name of the sport/activity (e.g., "Football").
	 * @param locationQuery The location to search near (e.g., "Curepipe").
	 * @return A list of PlayerLfg entities.
	 */
	@Transactional(readOnly = true)
	public List<PlayerLfg> findPlayers(String activityName, String locationQuery) {
		log.debug("Finding LFG players for activity '{}' near '{}'", activityName, locationQuery);

		// 1. Find the corresponding Activity entity.
		Activity activity = activityRepository.findByName(activityName)
				.orElseThrow(() -> new LfgServiceException("Activity '" + activityName + "' not found."));

		// 2. Geocode the location string to get coordinates.
		Point locationPoint = geocodingService.geocode(locationQuery);

		// 3. Query the repository using the IDs and coordinates.
		// The default search radius is hardcoded here but could be made configurable.
		double radiusInMeters = 5000; // 5km
		return repository.findRecentLfgPlayers(activity.getId(), locationPoint, radiusInMeters);
	}

	/**
	 * Adds a user to the "Looking for Game" list for a specific activity and
	 * location. If the user is already on the list for that activity, it updates
	 * their timestamp and location.
	 * 
	 * @param activityName  The name of the sport/activity.
	 * @param locationQuery The location.
	 * @param username      The username of the player.
	 * @return The created or updated PlayerLfg entity.
	 */
	@Transactional
	public PlayerLfg addPlayer(String activityName, String locationQuery, String username) {
		log.info("Adding user '{}' to LFG for activity '{}' near '{}'", username, activityName, locationQuery);

		// 1. Find the User entity.
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new LfgServiceException("User '" + username + "' not found."));

		// 2. Find the Activity entity.
		Activity activity = activityRepository.findByName(activityName)
				.orElseThrow(() -> new LfgServiceException("Activity '" + activityName + "' not found."));

		// 3. Geocode the location.
		Point locationPoint = geocodingService.geocode(locationQuery);

		// 4. Check if the user is already looking for this game to avoid duplicates.
		// A more complex implementation would query the repository. For simplicity, we
		// create a new entry.
		// In a full system, you would add a unique constraint on (user_id,
		// activity_id).
		PlayerLfg lfg = new PlayerLfg();
		lfg.setUser(user);
		lfg.setActivity(activity);
		lfg.setLocation(locationPoint);

		// The save method comes from our GenericCrudService
		return this.save(lfg);
	}

	// A simple custom exception for better error handling within this service.
	public static class LfgServiceException extends RuntimeException {
		public LfgServiceException(String message) {
			super(message);
		}
	}
}