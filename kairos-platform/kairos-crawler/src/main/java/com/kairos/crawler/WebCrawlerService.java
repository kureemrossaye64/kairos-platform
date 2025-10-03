package com.kairos.crawler;


import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kairos.core.crawler.CrawlerService;
import com.kairos.core.crawler.DocumentExtraction;
import com.kairos.core.crawler.UrlToCrawl;
import com.kairos.crawler.CrawlController.WebCrawlerFactory;
import com.kairos.crawler.fetcher.PageFetcher;
import com.kairos.crawler.robotstxt.RobotstxtConfig;
import com.kairos.crawler.robotstxt.RobotstxtServer;
import com.kairos.crawler.spider.KairoSpider;
import com.kairos.crawler.util.Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebCrawlerService implements CrawlerService{

    @Value("${kairos.crawler.user-agent}")
    private String userAgent;

    @Value("${kairos.crawler.crawl-delay-ms}")
    private int crawlDelayMs;

    public List<DocumentExtraction> executeCrawl(UrlToCrawl toCrawl) {
    	String startUrl = toCrawl.url();
    	int maxDepth = toCrawl.depth();
        log.info("Executing Crawler4j crawl for URL: {} with max depth: {}", startUrl, maxDepth);

        // A temporary folder for the crawl's state data.
        File crawlStorageFolder = new File("crawl-data");
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder.getAbsolutePath());
        config.setMaxDepthOfCrawling(maxDepth);
        config.setPolitenessDelay(crawlDelayMs);
        config.setUserAgentString(userAgent);
        try {
        // Instantiate the controller for this crawl.
	        PageFetcher pageFetcher = new PageFetcher(config);
	        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        
        
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(startUrl);

            // Create an instance of our custom spider.
            KairoSpider spider = new KairoSpider(startUrl);
            
            

            // Start the crawl. This is a blocking operation.
            // We use only 1 thread because our worker is designed to handle one URL message at a time.
            controller.start(spider, 1);

            log.info("Crawler4j finished for URL: {}. Found {} documents.", startUrl, spider.getExtractions().size());
            return spider.getExtractions();
            
        } catch (Exception e) {
            log.error("Crawler4j execution failed for start URL: {}", startUrl, e);
            throw new RuntimeException("Crawl failed", e);
        }
    }

	@Override
	public boolean supports(UrlToCrawl urlToCrawl) {
		return !Util.isYouTubeUrl(urlToCrawl.url());
	}
}