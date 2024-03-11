package com.springboot.financial.service;

import com.springboot.financial.constants.CacheKey;
import com.springboot.financial.dto.Company;
import com.springboot.financial.dto.Dividend;
import com.springboot.financial.dto.ScrapedResult;
import com.springboot.financial.entity.CompanyEntity;
import com.springboot.financial.entity.DividendEntity;
import com.springboot.financial.exception.impl.NoCompanyException;
import com.springboot.financial.repository.CompanyRepository;
import com.springboot.financial.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    @Transactional(readOnly = true)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("Request received for company: {}", companyName);
        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository
                .findByName(companyName)
                .orElseThrow(() -> {
                    log.error("No company found with name: {}", companyName);
                    return new NoCompanyException();
                });

        log.info("Found company: {} with ID: {}, Ticker: {}", companyName, company.getId(), company.getTicker());

        // 2. 조회된 회사 ID로 배당금 정보 조회 
        List<DividendEntity> dividendEntities = this.dividendRepository
                .findAllByCompanyId(company.getId());

        log.info("Found {} dividend(s) for company: {}", dividendEntities.size(), companyName);

        // 3. 결과 조합 후 반환
        return new ScrapedResult(
                new Company(company.getTicker(), company.getName())
                ,
                dividendEntities.stream().map(dividendEntity ->
                        new Dividend(dividendEntity.getDate(), dividendEntity.getDividend())
                ).collect(Collectors.toList())
        );
    }
}
