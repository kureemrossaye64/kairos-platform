package com.kairos.sports_atlas.repositories;

import java.util.List;
import java.util.Optional;

import com.kairos.sports_atlas.entities.Facility;

public interface FacilityRepository extends ManifestableRepository<Facility> {
	// List<Facility> findByTypeAndLocation(String type, String location);

	/*
	 * @Query(value = "SELECT f.* FROM facilities f " +
	 * "JOIN facility_activities fa ON f.id = fa.facility_id " +
	 * "JOIN activities a ON fa.activity_id = a.id " +
	 * "WHERE a.name LIKE %:activityName% AND ST_DWithin(f.coordinates, :location, :radiusInMeters)"
	 * , nativeQuery = true) List<Facility>
	 * findNearbyFacilitiesByActivity(@Param("location") Point location,
	 * 
	 * @Param("activityName") String activityName, @Param("radiusInMeters") double
	 * radiusInMeters);
	 */

	/*
	 * @Query(value = "SELECT f.* FROM facilities f " +
	 * "JOIN facility_activities fa ON f.id = fa.facility_id " +
	 * "JOIN activities a ON fa.activity_id = a.id " + // This is the crucial part:
	 * ensure no conflicting bookings exist "WHERE a.name = :activityName " +
	 * "AND ST_DWithin(f.coordinates, :location, :radiusInMeters) " +
	 * "AND NOT EXISTS (" + "    SELECT 1 FROM bookings b " +
	 * "    WHERE b.facility_id = f.id " +
	 * "    AND (b.start_time, b.end_time) OVERLAPS (CAST(:startTime AS timestamp), CAST(:endTime AS timestamp))"
	 * + ")", nativeQuery = true) //List<Facility>
	 * findAvailableNearbyFacilitiesByActivity(@Param("location") Point location,
	 * 
	 * @Param("activityName") String activityName, @Param("radiusInMeters") double
	 * radiusInMeters,
	 * 
	 * @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime
	 * endTime);
	 */
	Optional<Facility> findByNameIgnoreCase(String rawInput);

	List<Facility> findByNameContainingIgnoreCase(String rawInput);
	
	/**
     * Given a list of facility IDs, this query returns a sub-list containing only those
     * that have NO booking conflicts during the specified time window.
     * This is the "Availability Pruning" query.
     *
     * @param candidateIds The list of potential facility IDs from the search engine.
     * @param startTime The desired start time for the booking.
     * @param endTime The desired end time for the booking.
     * @return A list of Facility entities that are confirmed to be available.
     */
	/*
	 * @Query("SELECT f FROM Facility f WHERE f.id IN :candidateIds AND NOT EXISTS ("
	 * + "SELECT b FROM Booking b WHERE b.facility = f AND " +
	 * "(b.startTime < :endTime AND b.endTime > :startTime)" + // Standard overlap
	 * check ")") List<Facility> findAvailableFacilitiesFromList(
	 * 
	 * @Param("candidateIds") List<UUID> candidateIds,
	 * 
	 * @Param("startTime") LocalDateTime startTime,
	 * 
	 * @Param("endTime") LocalDateTime endTime );
	 */
}