package com.kairos.pulsberry;

import org.springframework.boot.SpringApplication;

import com.kairos.autoconfigure.EnableKairos;


@EnableKairos
public class PulseBerrySpringApplication {
	
	public static void main(String[] args) {
        SpringApplication.run(PulseBerrySpringApplication.class, args);
    }

}
