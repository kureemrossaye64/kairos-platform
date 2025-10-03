package com.kairos.core.crawler;

import java.time.LocalDateTime;

import com.kairos.core.BaseObject;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)

public class CrawledUrl extends BaseObject {

    private String url;

    private int urlHash; // Store a hash for efficient lookups

    private int httpStatusCode;

    private String contentType;

    private String contentHash; // Hash of the content to detect changes

    private LocalDateTime lastCrawled;

    public CrawledUrl(String url) {
        this.url = url;
        this.urlHash = url.hashCode();
        this.lastCrawled = LocalDateTime.now();
    }
    
    public static CrawledUrl from(String url) {
    	return new CrawledUrl(url);
    }
}