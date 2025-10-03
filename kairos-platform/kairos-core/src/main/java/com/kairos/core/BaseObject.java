package com.kairos.core;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;


@Data
public class BaseObject {
	
    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;


}
