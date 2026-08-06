package com.hrms.controller;

import com.hrms.dto.PayrollBulkGenerateRequest;
import com.hrms.dto.PayrollBulkGenerateResponse;
import com.hrms.dto.PayrollGenerateRequest;
import com.hrms.dto.PayrollResponse;
import com.hrms.entity.Payroll;
import com.hrms.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/generate")
    public ResponseEntity<PayrollResponse> generatePayroll(
            @Valid @RequestBody PayrollGenerateRequest request) {
        Payroll saved = payrollService.generatePayroll(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PayrollResponse.fromEntity(saved));
    }

    @PostMapping("/generate-all")
    public ResponseEntity<PayrollBulkGenerateResponse> generatePayrollForAll(
            @Valid @RequestBody PayrollBulkGenerateRequest request) {
        PayrollBulkGenerateResponse result =
                payrollService.generatePayrollForAllEmployees(request.getMonth(), request.getYear());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PayrollResponse>> getPayroll(
            @RequestParam(required = false) Long employeeId) {

        List<Payroll> records = (employeeId != null)
                ? payrollService.getPayrollByEmployee(employeeId)
                : payrollService.getAllPayroll();

        List<PayrollResponse> response = records.stream()
                .map(PayrollResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}