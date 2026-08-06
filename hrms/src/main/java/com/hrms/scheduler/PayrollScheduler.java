package com.hrms.scheduler;

import com.hrms.dto.PayrollBulkGenerateResponse;
import com.hrms.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollScheduler {

    private final PayrollService payrollService;

    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyPayrollForAllEmployees() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        int month = previousMonth.getMonthValue();
        int year = previousMonth.getYear();

        log.info("Starting scheduled payroll generation for {}/{}", month, year);

        PayrollBulkGenerateResponse result = payrollService.generatePayrollForAllEmployees(month, year);

        log.info("Scheduled payroll generation complete for {}/{}: {} generated, {} skipped",
                month, year, result.getGenerated(), result.getSkipped());
    }
}