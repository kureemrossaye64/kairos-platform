package com.kairos.pulsberry.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
public class JpaCrawlJob extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CrawlStatus status = CrawlStatus.PAUSED;

	@ElementCollection
    @CollectionTable(name = "seed_urls") // Optional: Customizes the table name for the collection
    @Column(name = "seed_url")
	private List<String> seedUrls;

	private int maxDepth = 5; // How many links deep to follow from the seed

	private LocalDateTime lastRun;
	
	public static JpaCrawlJob from(CrawlJob i) {
		JpaCrawlJob j = new JpaCrawlJob();
		j.setName(i.getName());
		j.setCreatedAt(i.getCreatedAt());
		j.setId(i.getId());
		j.setLastRun(i.getLastRun());
		j.setMaxDepth(i.getMaxDepth());
		j.setSeedUrls(i.getSeedUrls());
		j.setStatus(i.getStatus());
		j.setUpdatedAt(i.getUpdatedAt());
		return j;
	}
	
	
	public CrawlJob toDto() {
		CrawlJob j = new CrawlJob();
		j.setName(getName());
		j.setCreatedAt(getCreatedAt());
		j.setId(getId());
		j.setLastRun(getLastRun());
		j.setMaxDepth(getMaxDepth());
		j.setSeedUrls(getSeedUrls());
		j.setStatus(getStatus());
		j.setUpdatedAt(getUpdatedAt());
		return j;
	}
}

