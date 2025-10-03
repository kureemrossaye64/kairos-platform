package com.kairos.crawler;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawledUrl;
import com.kairos.core.crawler.CrawlerService;
import com.kairos.core.crawler.DocumentExtraction;
import com.kairos.core.crawler.UrlToCrawl;
import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.core.ingestion.IngestionRequest;
import com.kairos.crawler.config.RabbitMQConfig;
import com.kairos.crawler.models.MockMultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerWorker {

    private final CrawlJobPersistence crawlJobPersistence;
    private final IIngestionRouter ingestionRouter;
    private final List<CrawlerService> crawlerServices;
    

    @RabbitListener(queues = RabbitMQConfig.URL_QUEUE_NAME)
    @Transactional
    public void processUrl(UrlToCrawl urlToCrawl) {
        log.info("Worker received message to crawl: {}", urlToCrawl);

        if (crawlJobPersistence.isUrlHashCrawled(urlToCrawl.url().hashCode())) {
            log.debug("URL {} has already been crawled recently. Acknowledging and skipping.", urlToCrawl.url());
            return;
        }
        
        for(CrawlerService service : crawlerServices) {
        	if(service.supports(urlToCrawl)) {
        		try {
                    List<DocumentExtraction> extractions = service.executeCrawl(urlToCrawl);

                    for (DocumentExtraction doc : extractions) {
                        Map<String, Object> manifest = new HashMap<>();
                        manifest.put("source_crawler_url", doc.getReference());
                        manifest.put("document_title", doc.getMetadata().getOrDefault("title", List.of("Untitled")).get(0));

                        InputStream contentStream = new ByteArrayInputStream(doc.getContent().getBytes(StandardCharsets.UTF_8));
                        String contentType = doc.getMetadata().getOrDefault("Content-Type", List.of("text/html")).get(0);
                        
                        IngestionRequest request = IngestionRequest.from(
                            new MockMultipartFile(doc.getReference(), contentType, contentStream),
                            manifest
                        );
                        
                        ingestionRouter.addRequest(request);
                        log.info("Submitted document from {} to ingestion pipeline.", doc.getReference());
                    }

                    crawlJobPersistence.saveOrUpdateCrawledUrl(new CrawledUrl(urlToCrawl.url()));

                } catch (Exception e) {
                    log.error("Crawl failed for URL: {}. Message will be requeued if possible.", urlToCrawl.url(), e);
                    throw new RuntimeException("Crawl failed", e);
                }
        	}
        }
    }
}