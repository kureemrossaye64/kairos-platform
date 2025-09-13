package com.kairos.sports_atlas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.Booking;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}