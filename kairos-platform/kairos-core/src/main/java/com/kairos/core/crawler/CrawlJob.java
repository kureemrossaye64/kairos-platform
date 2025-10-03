package com.kairos.core.crawler;

import java.time.LocalDateTime;
import java.util.List;

import com.kairos.core.BaseObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrawlJob extends BaseObject {

	private String name;

	private CrawlStatus status = CrawlStatus.PAUSED;

	private List<String> seedUrls;

	private int maxDepth = 5; // How many links deep to follow from the seed

	private LocalDateTime lastRun;
}

