package com.kairos.core.crawler;

import java.util.List;

public interface CrawlJobPersistence {
	
	public boolean isUrlHashCrawled(int urlHash);
	
	public CrawledUrl saveOrUpdateCrawledUrl(CrawledUrl crawledUrl);
	
	List<CrawlJob> getActiveJobs();
	
	public List<CrawlJob> getAllJobs();
	
	public CrawlJob saveOrUpdateCrawlJob(CrawlJob job);

}
