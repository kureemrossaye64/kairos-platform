package com.kairos.crawler.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlStatus;
import com.kairos.core.crawler.CrawledUrl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryCrawlJobPersistence implements CrawlJobPersistence {

    private final Map<UUID, CrawlJob> jobStore = new ConcurrentHashMap<>();
    // Using a separate map for URLs to ensure fast lookups
    private final Map<UUID, CrawledUrl> urlStore = new ConcurrentHashMap<>();
    // Optimization: Cache hashes for fast existence checks
    private final Set<Integer> urlHashes = Collections.synchronizedSet(new HashSet<>());

    private final ObjectMapper mapper = new ObjectMapper();
    
    // Persistence files
    private final File jobsFile = new File("./data/crawl-jobs.json");
    private final File urlsFile = new File("./data/crawled-urls.json");

    public InMemoryCrawlJobPersistence() {
        // Register JavaTimeModule for LocalDateTime support if not auto-registered
        mapper.findAndRegisterModules();
    }

    @Override
    public boolean isUrlHashCrawled(int urlHash) {
        return urlHashes.contains(urlHash);
    }

    @Override
    public CrawledUrl saveOrUpdateCrawledUrl(CrawledUrl crawledUrl) {
        if (crawledUrl.getId() == null) {
            crawledUrl.setId(UUID.randomUUID());
            crawledUrl.setCreatedAt(Instant.now());
        }
        crawledUrl.setUpdatedAt(Instant.now());
        
        urlStore.put(crawledUrl.getId(), crawledUrl);
        urlHashes.add(crawledUrl.getUrlHash());
        
        return crawledUrl;
    }

    @Override
    public List<CrawlJob> getActiveJobs() {
        return jobStore.values().stream()
                .filter(job -> job.getStatus() == CrawlStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public List<CrawlJob> getAllJobs() {
        return new ArrayList<>(jobStore.values());
    }

    @Override
    public CrawlJob saveOrUpdateCrawlJob(CrawlJob job) {
        if (job.getId() == null) {
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.now());
        }
        job.setUpdatedAt(Instant.now());
        jobStore.put(job.getId(), job);
        return job;
    }

    // --- Persistence Logic ---

    @PostConstruct
    public void loadData() {
        loadJobs();
        loadUrls();
    }

    private void loadJobs() {
        if (jobsFile.exists()) {
            try {
                List<CrawlJob> loaded = mapper.readValue(jobsFile, new TypeReference<List<CrawlJob>>() {});
                loaded.forEach(j -> jobStore.put(j.getId(), j));
                log.info("Loaded {} CrawlJobs from disk.", loaded.size());
            } catch (IOException e) {
                log.warn("Failed to load CrawlJobs", e);
            }
        }
    }

    private void loadUrls() {
        if (urlsFile.exists()) {
            try {
                List<CrawledUrl> loaded = mapper.readValue(urlsFile, new TypeReference<List<CrawledUrl>>() {});
                loaded.forEach(u -> {
                    urlStore.put(u.getId(), u);
                    urlHashes.add(u.getUrlHash());
                });
                log.info("Loaded {} CrawledUrls from disk.", loaded.size());
            } catch (IOException e) {
                log.warn("Failed to load CrawledUrls", e);
            }
        }
    }

    @PreDestroy
    public void saveData() {
        try {
            if (!jobsFile.getParentFile().exists()) jobsFile.getParentFile().mkdirs();
            
            mapper.writeValue(jobsFile, new ArrayList<>(jobStore.values()));
            mapper.writeValue(urlsFile, new ArrayList<>(urlStore.values()));
            
            log.info("Persisted Crawler data to disk.");
        } catch (IOException e) {
            log.error("Failed to save Crawler data", e);
        }
    }
}