package com.kairos.pulsberry.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlStatus;
import com.kairos.core.crawler.CrawledUrl;
import com.kairos.pulsberry.entity.JpaCrawlJob;
import com.kairos.pulsberry.entity.JpaCrawledUrl;
import com.kairos.pulsberry.repositories.CrawlJobRepository;
import com.kairos.pulsberry.repositories.CrawledUrlRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JpaCrawJobPersistence implements CrawlJobPersistence{
	
	private final CrawledUrlRepository crawledUrlRepository;
	
	private final CrawlJobRepository crawlJobRepository;

	@Override
	public boolean isUrlHashCrawled(int urlHash) {
		return crawledUrlRepository.existsByUrlHash(urlHash);
	}

	@Override
	public CrawledUrl saveOrUpdateCrawledUrl(CrawledUrl crawledUrl) {
		
		JpaCrawledUrl j = JpaCrawledUrl.from(crawledUrl);
		j = crawledUrlRepository.save(j);
		return j.toDto();
	}

	@Override
	public List<CrawlJob> getActiveJobs() {
		return crawlJobRepository.findByStatus(CrawlStatus.ACTIVE).stream().map(m -> m.toDto()).collect(Collectors.toList());
	}

	@Override
	public CrawlJob saveOrUpdateCrawlJob(CrawlJob job) {
		return crawlJobRepository.save(JpaCrawlJob.from(job)).toDto();
	}

	@Override
	public List<CrawlJob> getAllJobs() {
		return crawlJobRepository.findAllByOrderByIdDesc().stream().map(m -> m.toDto()).collect(Collectors.toList());
	}

}
