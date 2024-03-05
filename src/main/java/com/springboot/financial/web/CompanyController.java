package com.springboot.financial.web;

import com.springboot.financial.model.Company;
import com.springboot.financial.service.CompanyService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(
            @RequestParam String keyword
    ) {
        return null;
    }

    @GetMapping()
    public ResponseEntity<?> searchCompany() {
        return null;
    }

    /**
     * 회사 및 배당금 정보 조회
     *
     * @param request
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> addCompany(
            @RequestBody Company request
    ) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }
        Company company = this.companyService.save(ticker);


        return ResponseEntity.ok(company);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteCompany() {
        return null;
    }
}
