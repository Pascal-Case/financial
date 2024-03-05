package com.springboot.financial.service;

import com.springboot.financial.model.Company;
import com.springboot.financial.model.Dividend;
import com.springboot.financial.model.ScrapedResult;
import com.springboot.financial.persist.CompanyRepository;
import com.springboot.financial.persist.DividendRepository;
import com.springboot.financial.persist.entity.CompanyEntity;
import com.springboot.financial.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository
                .findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

        // 2. 조회된 회사 ID로 배당금 정보 조회 
        List<DividendEntity> dividendEntities = this.dividendRepository
                .findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
        return new ScrapedResult(
                Company.builder()
                        .ticker(company.getTicker())
                        .name(company.getName())
                        .build(),
                dividendEntities.stream().map(dividendEntity ->
                        Dividend.builder()
                                .date(dividendEntity.getDate())
                                .dividend(dividendEntity.getDividend())
                                .build()
                ).collect(Collectors.toList())
        );
    }
}
