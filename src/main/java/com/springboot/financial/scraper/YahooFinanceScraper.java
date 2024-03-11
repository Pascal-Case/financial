package com.springboot.financial.scraper;

import com.springboot.financial.constants.Month;
import com.springboot.financial.dto.Company;
import com.springboot.financial.dto.Dividend;
import com.springboot.financial.dto.ScrapedResult;
import com.springboot.financial.exception.impl.InvalidTickerException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class YahooFinanceScraper implements Scraper {
    private static final String STATISTIC_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String PROFILE_URL = "https://finance.yahoo.com/quote/%s/profile";
    private static final long START_TIME = 86400; // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company) {
        log.info("Scraping financial data for company: {}", company.getTicker());
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);
        try {
            long now = System.currentTimeMillis() / 1000;
            String url = String.format(STATISTIC_URL, company.getTicker(), START_TIME, now);
            log.info("Connecting to URL: {}", url);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tablesElement = parsingDivs.get(0);

            Element tbody = tablesElement.children().get(1);
            List<Dividend> dividendList = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }
                dividendList.add(
                        new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend)
                );
            }
            scrapResult.setDividends(dividendList);

        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }

        log.info("Scraping completed for company: {}", company.getTicker());
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        log.info("Scraping company profile for ticker: {}", ticker);
        String url = String.format(PROFILE_URL, ticker);
        try {
            log.debug("Connecting to URL: {}", url);
            Document document = Jsoup.connect(url).get();

            Elements titleElements = document.getElementsByTag("h1");

            if (titleElements.isEmpty()) {
                log.error("No company found for ticker: {}", ticker);
                throw new InvalidTickerException();
            }

            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split("\\(")[0].trim();
            log.info("Scraping completed for ticker: {}", ticker);
            return new Company(ticker, title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
