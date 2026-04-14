package com.kairos.crawler.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.crawler.CrawlJob;
import com.kairos.core.crawler.CrawlJobPersistence;
import com.kairos.core.crawler.CrawlStatus;
import com.kairos.core.crawler.CrawledUrl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class JdbcCrawlJobPersistence implements CrawlJobPersistence {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<CrawlJob> jobRowMapper;
    
    

    public JdbcCrawlJobPersistence(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		super();
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
		this.jobRowMapper = (rs, rowNum) -> {
	        CrawlJob j = new CrawlJob();
	        j.setId(UUID.fromString(rs.getString("id")));
	        j.setName(rs.getString("name"));
	        j.setStatus(CrawlStatus.valueOf(rs.getString("status")));
	        j.setMaxDepth(rs.getInt("max_depth"));
	        
	        Timestamp lastRun = rs.getTimestamp("last_run");
	        if(lastRun != null) j.setLastRun(lastRun.toLocalDateTime());
	        
	        j.setCreatedAt(rs.getTimestamp("created_at").toInstant());
	        j.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());

	        try {
	            String seeds = rs.getString("seed_urls");
	            if (seeds != null) {
	                j.setSeedUrls(objectMapper.readValue(seeds, new TypeReference<List<String>>() {}));
	            } else {
	                j.setSeedUrls(new ArrayList<>());
	            }
	        } catch (IOException e) {
	            log.error("Failed to parse seed URLs JSON", e);
	        }
	        return j;
	    };
	}

	@PostConstruct
    public void initTables() {
        log.info("Initializing JDBC Tables for Crawler...");
        
        // 1. Table for Crawl Jobs
        String sqlJobs = """
            CREATE TABLE IF NOT EXISTS crawl_jobs (
                id UUID PRIMARY KEY,
                name VARCHAR(255) UNIQUE,
                status VARCHAR(50),
                seed_urls TEXT,
                max_depth INT,
                last_run TIMESTAMP,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
        """;
        jdbcTemplate.execute(sqlJobs);

        // 2. Table for Crawled URLs (History)
        String sqlUrls = """
            CREATE TABLE IF NOT EXISTS crawled_urls (
                id UUID PRIMARY KEY,
                url TEXT NOT NULL,
                url_hash INT NOT NULL,
                http_status_code INT,
                content_type VARCHAR(255),
                content_hash VARCHAR(255),
                last_crawled TIMESTAMP,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
        """;
        jdbcTemplate.execute(sqlUrls);
        
        // Index for fast hash lookup
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_crawled_url_hash ON crawled_urls(url_hash)");
    }

    @Override
    public boolean isUrlHashCrawled(int urlHash) {
        String sql = "SELECT count(*) FROM crawled_urls WHERE url_hash = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, urlHash);
        return count != null && count > 0;
    }

    @Override
    public CrawledUrl saveOrUpdateCrawledUrl(CrawledUrl crawledUrl) {
        if (crawledUrl.getId() == null) {
            crawledUrl.setId(UUID.randomUUID());
            crawledUrl.setCreatedAt(Instant.now());
            return insertUrl(crawledUrl);
        } else {
            return updateUrl(crawledUrl);
        }
    }

    private CrawledUrl insertUrl(CrawledUrl u) {
        u.setUpdatedAt(Instant.now());
        String sql = """
            INSERT INTO crawled_urls (id, url, url_hash, http_status_code, content_type, content_hash, last_crawled, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, u.getId(), u.getUrl(), u.getUrlHash(), u.getHttpStatusCode(), u.getContentType(), u.getContentHash(), 
                toTs(u.getLastCrawled()), toTs(u.getCreatedAt()), toTs(u.getUpdatedAt()));
        return u;
    }

    private CrawledUrl updateUrl(CrawledUrl u) {
        u.setUpdatedAt(Instant.now());
        String sql = """
            UPDATE crawled_urls SET url=?, url_hash=?, http_status_code=?, content_type=?, content_hash=?, last_crawled=?, updated_at=?
            WHERE id=?
        """;
        int updated = jdbcTemplate.update(sql, u.getUrl(), u.getUrlHash(), u.getHttpStatusCode(), u.getContentType(), u.getContentHash(), 
                toTs(u.getLastCrawled()), toTs(u.getUpdatedAt()), u.getId());
        
        if (updated == 0) return insertUrl(u);
        return u;
    }

    @Override
    public CrawlJob saveOrUpdateCrawlJob(CrawlJob job) {
        if (job.getId() == null) {
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.now());
            return insertJob(job);
        } else {
            return updateJob(job);
        }
    }

    private CrawlJob insertJob(CrawlJob j) {
        j.setUpdatedAt(Instant.now());
        String sql = """
            INSERT INTO crawl_jobs (id, name, status, seed_urls, max_depth, last_run, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, j.getId(), j.getName(), j.getStatus().name(), toJson(j.getSeedUrls()), j.getMaxDepth(), 
                toTs(j.getLastRun()), toTs(j.getCreatedAt()), toTs(j.getUpdatedAt()));
        return j;
    }

    private CrawlJob updateJob(CrawlJob j) {
        j.setUpdatedAt(Instant.now());
        String sql = """
            UPDATE crawl_jobs SET name=?, status=?, seed_urls=?, max_depth=?, last_run=?, updated_at=?
            WHERE id=?
        """;
        int updated = jdbcTemplate.update(sql, j.getName(), j.getStatus().name(), toJson(j.getSeedUrls()), j.getMaxDepth(), 
                toTs(j.getLastRun()), toTs(j.getUpdatedAt()), j.getId());
        
        if (updated == 0) return insertJob(j);
        return j;
    }

    @Override
    public List<CrawlJob> getActiveJobs() {
        String sql = "SELECT * FROM crawl_jobs WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(sql, jobRowMapper);
    }

    @Override
    public List<CrawlJob> getAllJobs() {
        String sql = "SELECT * FROM crawl_jobs ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, jobRowMapper);
    }

    // --- Helpers & Mappers ---

    

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private Timestamp toTs(Object time) {
        if (time == null) return null;
        if (time instanceof Instant) return Timestamp.from((Instant) time);
        if (time instanceof java.time.LocalDateTime) return Timestamp.valueOf((java.time.LocalDateTime) time);
        return null;
    }
}