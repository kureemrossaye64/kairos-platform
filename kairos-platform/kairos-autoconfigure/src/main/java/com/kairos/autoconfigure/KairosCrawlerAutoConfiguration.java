package com.kairos.autoconfigure;

import java.util.List;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.autoconfigure.properties.KairosProperties;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlerService;
import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.crawler.CrawlConfig;
import com.kairos.crawler.CrawlController;
import com.kairos.crawler.WebCrawlerService;
import com.kairos.crawler.config.RabbitMQConfig;
import com.kairos.crawler.execution.CrawlJobScheduler;
import com.kairos.crawler.execution.CrawlerWorker;
import com.kairos.crawler.execution.DistributedCrawlJobScheduler;
import com.kairos.crawler.execution.LocalCrawlJobScheduler;
import com.kairos.crawler.persistence.InMemoryCrawlJobPersistence;
import com.kairos.crawler.persistence.JdbcCrawlJobPersistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auto-configuration for Distributed Web Crawling.
 * <p>
 * Provides both local (in-memory) and distributed (RabbitMQ) crawling modes.
 * Distributed mode requires spring-boot-starter-amqp on classpath.
 */
@Slf4j
@AutoConfiguration(after = RabbitAutoConfiguration.class)
@ConditionalOnClass(CrawlController.class)
@ConditionalOnProperty(prefix = "kairos.crawler", name = "enabled", havingValue = "true")
@EnableScheduling
@RequiredArgsConstructor
public class KairosCrawlerAutoConfiguration {

    private final KairosProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public CrawlConfig crawlConfig() {
        KairosProperties.Crawler crawler = properties.getCrawler();
        CrawlConfig config = new CrawlConfig();
        config.setUserAgentString(crawler.getUserAgent());
        config.setPolitenessDelay(crawler.getPolitenessDelay());
        config.setMaxDepthOfCrawling(crawler.getMaxDepth());
        config.setMaxPagesToFetch(crawler.getMaxPagesToFetch());
        config.setCrawlStorageFolder(System.getProperty("java.io.tmpdir") + "/kairos-crawler");
        config.setResumableCrawling(false);
        config.setIncludeHttpsPages(true);
        return config;
    }
    
    
    @Bean
    public CrawlerService webCrawler(CrawlConfig config) {
    	return new WebCrawlerService(config);
    }
    
    
    @Bean
    @ConditionalOnProperty(prefix = "kairos.persistence", name = "provider", havingValue = "IN_MEMORY", matchIfMissing = true)
    @ConditionalOnMissingBean(CrawlJobPersistence.class)
    public CrawlJobPersistence inMemoryCrawlJobPersistence() {
    	log.info("CrawlJobPersistence used: IN_MEMORY");
        return new InMemoryCrawlJobPersistence();
    }

    @Bean
    @ConditionalOnProperty(prefix = "kairos.persistence", name = "provider", havingValue = "JDBC")
    @ConditionalOnMissingBean(CrawlJobPersistence.class)
    public CrawlJobPersistence jdbcCrawlJobPersistence(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    	log.info("CrawlJobPersistence used: JDBC");
        return new JdbcCrawlJobPersistence(jdbcTemplate, objectMapper);
    }

    

    @Bean
    @ConditionalOnMissingBean
    public CrawlerWorker crawlerWork(CrawlJobPersistence persistence, IIngestionRouter router, List<CrawlerService> services) {
        return new CrawlerWorker(persistence, router, services);
    }
    
    
    

    /**
     * In-memory crawler worker for non-distributed mode.
     * Only active when RabbitTemplate is NOT available.
     */
    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "kairos.crawler", name = "distributed", havingValue = "false", matchIfMissing = true)
    public CrawlJobScheduler localCrawlerScheduler(CrawlJobPersistence persistence,CrawlerWorker worker) {
        log.info("CrawlJobScheduler used: LOCAL");
        return new LocalCrawlJobScheduler(persistence, worker);
    }
    

    @Bean
    @ConditionalOnProperty(prefix = "kairos.crawler", name = "distributed", havingValue = "true")
    public Queue urlQueue() {
        return new Queue(RabbitMQConfig.URL_QUEUE_NAME, true);
    }

    /**
     * A message converter that automatically serializes/deserializes objects to/from JSON.
     * This allows us to send rich message objects instead of just strings.
     */
    @Bean
    @ConditionalOnProperty(prefix = "kairos.crawler", name = "distributed", havingValue = "true")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    
    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "kairos.crawler", name = "distributed", havingValue = "true")
    public CrawlJobScheduler distributedCrawlerScheduler(CrawlJobPersistence persistence,RabbitTemplate rabbitTemplate, CrawlerWorker worker) {
    	log.info("CrawlJobScheduler used: Distributed");
        return new DistributedCrawlJobScheduler(persistence, rabbitTemplate,worker);
    }
   

    
}