package com.springboot.financial.web;

import com.springboot.financial.annotation.ReadRoleAuthorize;
import com.springboot.financial.annotation.WriteRoleAuthorize;
import com.springboot.financial.model.Company;
import com.springboot.financial.model.constants.CacheKey;
import com.springboot.financial.persist.entity.CompanyEntity;
import com.springboot.financial.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    @ReadRoleAuthorize
    public ResponseEntity<?> autocomplete(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(this.companyService.autoComplete(keyword));
    }

    @GetMapping()
    @ReadRoleAuthorize
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companyList = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companyList);
    }

    @PostMapping()
    @WriteRoleAuthorize
    public ResponseEntity<?> addCompany(
            @RequestBody Company request
    ) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }
        Company company = this.companyService.save(ticker);

        // 자동완성 키워드 등록
        this.companyService.addAutoCompleteKeyword(company.getName());

        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @WriteRoleAuthorize
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);

        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        Objects.requireNonNull(this.redisCacheManager.getCache(CacheKey.KEY_FINANCE)).evict(companyName);
    }
}
