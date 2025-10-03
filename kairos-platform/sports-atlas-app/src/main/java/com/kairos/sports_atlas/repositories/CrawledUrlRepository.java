package com.kairos.sports_atlas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.sports_atlas.entities.JpaCrawledUrl;

public interface CrawledUrlRepository extends JpaRepository<JpaCrawledUrl, UUID> {
    boolean existsByUrlHash(int urlHash);
}