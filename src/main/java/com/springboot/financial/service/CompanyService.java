package com.springboot.financial.service;

import com.springboot.financial.dto.Company;
import com.springboot.financial.dto.ScrapedResult;
import com.springboot.financial.entity.CompanyEntity;
import com.springboot.financial.entity.DividendEntity;
import com.springboot.financial.exception.impl.AlreadyExistTickerException;
import com.springboot.financial.exception.impl.NoCompanyException;
import com.springboot.financial.exception.impl.ScrapFailedException;
import com.springboot.financial.repository.CompanyRepository;
import com.springboot.financial.repository.DividendRepository;
import com.springboot.financial.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie<String, String> trie;
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        log.info("Checking if ticker {} already exists.", ticker);
        boolean exist = companyRepository.existsByTicker(ticker);
        if (exist) {
            throw new AlreadyExistTickerException();
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        log.info("Starting to scrape company information for ticker: {}", ticker);
        // ticker 기준으로 회사를 스크래핑
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new ScrapFailedException();
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        log.info("Scraping dividends for company: {}", company.getName());
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        log.info("Saving company and dividends information to the database for company: {}", company.getName());
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(dividend -> new DividendEntity(companyEntity.getId(), dividend))
                .toList();
        this.dividendRepository.saveAll(dividendEntityList);

        log.info("Company and dividends saved successfully for company: {}", company.getName());
        return company;
    }

    public void addAutoCompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autoComplete(String keyword) {
        return this.trie.prefixMap(keyword).keySet().stream().limit(10).toList();
    }

    public void deleteAutoCompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(NoCompanyException::new);

        log.info("Deleting all dividends associated with company ID: {}", company.getId());
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        log.info("Deleting autocomplete keyword for company: {}", company.getName());
        this.deleteAutoCompleteKeyword(company.getName());
        return company.getName();
    }
}
