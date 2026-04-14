package com.kairos.pulsberry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.kairos")
@EnableJpaRepositories(basePackages = "com.kairos")
@EntityScan(basePackages = "com.kairos")
@EnableAsync
@EnableScheduling
public class PulseBerrySpringApplication {
	
	public static void main(String[] args) {
        SpringApplication.run(PulseBerrySpringApplication.class, args);
    }

}
