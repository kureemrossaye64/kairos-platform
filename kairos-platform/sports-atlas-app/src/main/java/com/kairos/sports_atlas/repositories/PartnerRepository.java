package com.kairos.sports_atlas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.Partner;

public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    // We can add custom query methods here later if needed, e.g., findByNameContainingIgnoreCase(String name)
}