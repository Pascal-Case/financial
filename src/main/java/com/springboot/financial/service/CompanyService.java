package com.springboot.financial.service;

import com.springboot.financial.model.Company;
import com.springboot.financial.model.ScrapedResult;
import com.springboot.financial.persist.CompanyRepository;
import com.springboot.financial.persist.DividendRepository;
import com.springboot.financial.persist.entity.CompanyEntity;
import com.springboot.financial.persist.entity.DividendEntity;
import com.springboot.financial.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exist = companyRepository.existsByTicker(ticker);
        if (exist) {
            throw new RuntimeException("already exist ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker 기준으로 회사를 스크래핑
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        } else {
            System.out.println("not empty !!" + company.toString());
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(dividend -> new DividendEntity(companyEntity.getId(), dividend))
                .toList();
        this.dividendRepository.saveAll(dividendEntityList);

        return company;
    }
}
