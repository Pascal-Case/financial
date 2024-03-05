package com.springboot.financial.scraper;

import com.springboot.financial.model.Company;
import com.springboot.financial.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);
}
