package com.springboot.financial.web;

import com.springboot.financial.annotation.ReadRoleAuthorize;
import com.springboot.financial.annotation.WriteRoleAuthorize;
import com.springboot.financial.exception.impl.TickerNotEnteredException;
import com.springboot.financial.model.Company;
import com.springboot.financial.model.constants.CacheKey;
import com.springboot.financial.persist.entity.CompanyEntity;
import com.springboot.financial.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    @ReadRoleAuthorize
    public ResponseEntity<?> autocomplete(
            @RequestParam String keyword
    ) {
        log.info("Autocompleting for keyword: {}", keyword);
        return ResponseEntity.ok(this.companyService.autoComplete(keyword));
    }

    @GetMapping()
    @ReadRoleAuthorize
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        log.info("Fetching all companies with pageable: {}", pageable);
        Page<CompanyEntity> companyList = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companyList);
    }

    @PostMapping()
    @WriteRoleAuthorize
    public ResponseEntity<?> addCompany(
            @RequestBody Company request
    ) {
        log.info("Adding new company with ticker: {}", request.getTicker());
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            log.warn("Attempted to add company with empty ticker");
            throw new TickerNotEnteredException();
        }
        Company company = this.companyService.save(ticker);
        log.info("Company added successfully: {}", company);

        // 자동완성 키워드 등록
        this.companyService.addAutoCompleteKeyword(company.getName());
        log.info("Autocomplete keyword added for company: {}", company.getName());

        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @WriteRoleAuthorize
    public ResponseEntity<?> deleteCompany(
            @PathVariable String ticker
    ) {
        log.info("Deleting company with ticker: {}", ticker);
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);
        log.info("Company deleted successfully: {}", companyName);

        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        log.info("Clearing finance cache for company: {}", companyName);
        Objects.requireNonNull(this.redisCacheManager.getCache(CacheKey.KEY_FINANCE)).evict(companyName);
        log.info("Finance cache cleared for company: {}", companyName);
    }
}
