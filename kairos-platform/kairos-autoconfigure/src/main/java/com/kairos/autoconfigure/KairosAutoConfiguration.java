package com.kairos.autoconfigure;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kairos.autoconfigure.properties.KairosProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Core auto-configuration for KAIROS Platform.
 * <p>
 * This class orchestrates the loading of feature-specific configurations based on
 * classpath availability and user properties.
 */

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "kairos", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    KairosAIAutoConfiguration.class,
    KairosVectorStoreAutoConfiguration.class,
    KairosCrawlerAutoConfiguration.class,
    KairosIngestionAutoConfiguration.class,
    KairosAgenticAutoConfiguration.class ,
    KairosStorageAutoConfiguration.class
})
public class KairosAutoConfiguration {

    private final KairosProperties properties;

    public KairosAutoConfiguration(KairosProperties properties) {
        this.properties = properties;
    }
    
    

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup(ApplicationReadyEvent event) {
        log.info("╔═══════════════════════════════════════════════════════╗");
        log.info("║	KAIROS Platform Initialized			║");
        log.info("║	Version: 1.0.0 | Mode: {}		║", properties.isDevMode() ? "DEVELOPMENT" : "PRODUCTION");
        log.info("║	Vector Store: {}				║", properties.getVector().getProvider());
        log.info("║	Storage Provider: {}				║", properties.getStorage().getProvider());
        log.info("║	Persistence Provider: {}			║", properties.getPersistence().getProvider());
        log.info("║	Crawler: {}					║", properties.getCrawler().isDistributed()? "Distributed":"Local");
        log.info("╚═══════════════════════════════════════════════════════╝");
    }
}