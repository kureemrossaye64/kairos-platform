package com.kairos.crawler.parser;

import com.kairos.crawler.Page;
import com.kairos.crawler.exceptions.ParseException;

public interface HtmlParser {

    HtmlParseData parse(Page page, String contextURL) throws ParseException;

}
