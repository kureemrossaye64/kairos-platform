package com.kairos.cultural_archive.repository;

import com.kairos.cultural_archive.entity.CulturalAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CulturalAssetRepository extends JpaRepository<CulturalAsset, UUID> {
}