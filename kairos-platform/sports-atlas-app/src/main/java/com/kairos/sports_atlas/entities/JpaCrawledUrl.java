package com.kairos.sports_atlas.entities;

import java.time.LocalDateTime;

import com.kairos.core.crawler.CrawledUrl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crawled_urls", indexes = {
    @Index(name = "idx_crawled_url_url_hash", columnList = "urlHash", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class JpaCrawledUrl extends BaseEntity {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(nullable = false)
    private int urlHash; // Store a hash for efficient lookups

    private int httpStatusCode;

    @Column(columnDefinition = "TEXT")
    private String contentType;

    private String contentHash; // Hash of the content to detect changes

    private LocalDateTime lastCrawled;

    public JpaCrawledUrl(String url) {
        this.url = url;
        this.urlHash = url.hashCode();
        this.lastCrawled = LocalDateTime.now();
    }
    
    public static JpaCrawledUrl from(CrawledUrl u) {
    	JpaCrawledUrl r = new JpaCrawledUrl();
    	r.setContentHash(u.getContentHash());
    	r.setContentType(u.getContentType());
    	r.setCreatedAt(u.getCreatedAt());
    	r.setHttpStatusCode(u.getHttpStatusCode());
    	r.setId(u.getId());
    	r.setLastCrawled(u.getLastCrawled());
    	r.setUpdatedAt(u.getUpdatedAt());
    	r.setUrl(u.getUrl());
    	r.setUrlHash(u.getUrlHash());
    	return r;
    }
    
    public CrawledUrl toDto() {
    	
    	CrawledUrl r = new CrawledUrl(url);
    	r.setContentHash(getContentHash());
    	r.setContentType(getContentType());
    	r.setCreatedAt(getCreatedAt());
    	r.setHttpStatusCode(getHttpStatusCode());
    	r.setId(getId());
    	r.setLastCrawled(getLastCrawled());
    	r.setUpdatedAt(getUpdatedAt());
    	r.setUrl(getUrl());
    	r.setUrlHash(getUrlHash());
    	return r;
    }
}