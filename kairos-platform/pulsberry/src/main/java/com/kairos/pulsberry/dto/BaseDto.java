package com.kairos.pulsberry.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDto {
	
    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;

}
