package com.kairos.pulsberry.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.pulsberry.entity.JpaCrawledUrl;

public interface CrawledUrlRepository extends JpaRepository<JpaCrawledUrl, UUID> {
    boolean existsByUrlHash(int urlHash);
}