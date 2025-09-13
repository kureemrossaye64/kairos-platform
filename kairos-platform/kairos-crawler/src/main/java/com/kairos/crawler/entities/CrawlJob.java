package com.kairos.crawler.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;

import com.kairos.core.entity.BaseEntity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crawl_jobs")
@Getter
@Setter
@NoArgsConstructor
public class CrawlJob extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CrawlStatus status = CrawlStatus.PAUSED;

	@Type(ListArrayType.class)
	@Column(columnDefinition = "text[]")
	private List<String> seedUrls;

	private int maxDepth = 5; // How many links deep to follow from the seed

	private LocalDateTime lastRun;
}

