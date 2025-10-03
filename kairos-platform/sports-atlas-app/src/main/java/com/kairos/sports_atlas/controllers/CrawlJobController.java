package com.kairos.sports_atlas.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlStatus;

import lombok.RequiredArgsConstructor;

// A simple DTO for the request body
record CrawlJobRequest(String name, List<String> seedUrls, int maxDepth) {}

@RestController
@RequestMapping("/api/v1/crawler/jobs")
@RequiredArgsConstructor

public class CrawlJobController {

    private final CrawlJobPersistence crawlerService;

    @GetMapping
    public ResponseEntity<List<CrawlJob>> getAllJobs() {
        // Return jobs in reverse chronological order
        return ResponseEntity.ok(crawlerService.getAllJobs());
    }

    @PostMapping
    public ResponseEntity<CrawlJob> createJob(@RequestBody CrawlJobRequest request) {
        if (request.name() == null || request.name().isBlank() || request.seedUrls() == null || request.seedUrls().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CrawlJob newJob = new CrawlJob();
        newJob.setName(request.name());
        newJob.setSeedUrls(request.seedUrls());
        newJob.setMaxDepth(request.maxDepth() > 0 ? request.maxDepth() : 2); // Default depth of 2
        newJob.setStatus(CrawlStatus.ACTIVE);
        // Status defaults to PAUSED
        return ResponseEntity.ok(crawlerService.saveOrUpdateCrawlJob(newJob));
    }
    
    // We will need to update the CrawlJobRepository to have this method:
    // List<CrawlJob> findAllByOrderByIdDesc();
}