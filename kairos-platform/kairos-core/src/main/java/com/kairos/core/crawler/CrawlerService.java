package com.kairos.core.crawler;

import java.util.List;

public interface CrawlerService {
	
	public List<DocumentExtraction> executeCrawl(UrlToCrawl urlToCrawl);
	
	
	public boolean supports(UrlToCrawl urlToCrawl);

}
