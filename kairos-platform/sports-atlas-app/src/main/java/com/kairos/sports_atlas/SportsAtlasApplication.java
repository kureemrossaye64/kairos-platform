package com.kairos.sports_atlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.kairos")
@EnableJpaRepositories(basePackages = "com.kairos")
@EntityScan(basePackages = "com.kairos")
@EnableAsync
public class SportsAtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsAtlasApplication.class, args);
    }
}