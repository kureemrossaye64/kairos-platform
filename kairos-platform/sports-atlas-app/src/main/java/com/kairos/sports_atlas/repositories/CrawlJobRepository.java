package com.kairos.sports_atlas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.core.crawler.CrawlStatus;
import com.kairos.sports_atlas.entities.JpaCrawlJob;

public interface CrawlJobRepository extends JpaRepository<JpaCrawlJob, UUID> {

	List<JpaCrawlJob> findByStatus(CrawlStatus active);

	List<JpaCrawlJob> findAllByOrderByIdDesc();

}
