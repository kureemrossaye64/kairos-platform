package com.kairos.crawler.execution;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlStatus;
import com.kairos.core.crawler.UrlToCrawl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LocalCrawlJobScheduler implements CrawlJobScheduler{

	private final CrawlJobPersistence crawlJobPersistence;
	private final CrawlerWorker worker;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void scheduleActiveJobs() {
        log.info("Checking for active crawl jobs to schedule...");
        List<CrawlJob> activeJobs = crawlJobPersistence.getActiveJobs();

        if (activeJobs.isEmpty()) {
            log.info("No active crawl jobs found.");
            return;
        }

        for (CrawlJob job : activeJobs) {
            log.info("Starting crawl job: '{}'", job.getName());
            job.setStatus(CrawlStatus.RUNNING);
            job.setLastRun(LocalDateTime.now());
            crawlJobPersistence.saveOrUpdateCrawlJob(job);
            for (String seedUrl : job.getSeedUrls()) {
                UrlToCrawl message = new UrlToCrawl(seedUrl, 0); // Start at depth 0
                worker.processUrl(message);
                log.debug("Published seed URL to queue: {}", seedUrl);
            }
            
        }
    }

}
