package com.hrms.controller;

import com.hrms.dto.CompanyCreateRequest;
import com.hrms.dto.CompanyResponse;
import com.hrms.entity.Company;
import com.hrms.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyCreateRequest request) {
        Company saved = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CompanyResponse.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<CompanyResponse> companies = companyService.getAllCompanies().stream()
                .map(CompanyResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(CompanyResponse.fromEntity(company));
    }
}