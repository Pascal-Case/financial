package com.springboot.financial.scraper;

import com.springboot.financial.dto.Company;
import com.springboot.financial.dto.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);
}
