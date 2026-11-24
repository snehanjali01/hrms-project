package com.hrms.service;

import com.hrms.dto.PayrollBulkGenerateResponse;
import com.hrms.dto.PayrollGenerateRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.Payroll;
import com.hrms.enums.AttendanceStatus;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.hrms.enums.LeaveStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public Payroll generatePayroll(PayrollGenerateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(
                        request.getEmployeeId(), request.getMonth(), request.getYear())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Payroll for employee " + request.getEmployeeId()
                                    + " has already been generated for " + request.getMonth() + "/" + request.getYear());
                });

        // Block generating payroll for the current month or any future month —
        // attendance data for those days doesn't exist yet, so deductions would be wrong.
        YearMonth requestedPeriod = YearMonth.of(request.getYear(), request.getMonth());
        YearMonth currentPeriod = YearMonth.now();
        if (!requestedPeriod.isBefore(currentPeriod)) {
            throw new InvalidRequestException(
                    "Cannot generate payroll for " + request.getMonth() + "/" + request.getYear()
                            + " — that month hasn't ended yet. Payroll can only be generated for completed months.");
        }

        YearMonth period = requestedPeriod;
        LocalDate periodStart = period.atDay(1);
        LocalDate periodEnd = period.atEndOfMonth();

        // Pay is based on working days (Mon-Fri); weekends carry no attendance, so they are never deducted
        Map<LocalDate, AttendanceStatus> statusByDate = new HashMap<>();
        attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetween(request.getEmployeeId(), periodStart, periodEnd)
                .forEach(a -> statusByDate.put(a.getAttendanceDate(), a.getStatus()));

        // Includes leave that started in an earlier month but runs into this one
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findOverlapping(request.getEmployeeId(), LeaveStatus.APPROVED, periodStart, periodEnd);

        int workingDays = 0;
        BigDecimal unpaidDays = BigDecimal.ZERO;
        for (LocalDate day = periodStart; !day.isAfter(periodEnd); day = day.plusDays(1)) {
            if (isWeekend(day)) {
                continue;
            }
            workingDays++;
            AttendanceStatus status = statusByDate.get(day);
            if (status == AttendanceStatus.PRESENT) {
                continue;
            }
            if (status == AttendanceStatus.HALF_DAY) {
                unpaidDays = unpaidDays.add(new BigDecimal("0.5"));
            } else if (!isOnApprovedLeave(day, approvedLeaves)) {
                unpaidDays = unpaidDays.add(BigDecimal.ONE);
            }
        }

        BigDecimal basicSalary = employee.getSalary();
        if (basicSalary == null) {
            throw new InvalidRequestException(
                    "Employee " + employee.getId() + " has no salary set. Set a salary before generating payroll.");
        }
        BigDecimal perDayRate = basicSalary.divide(
                BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
        BigDecimal deductions = perDayRate
                .multiply(unpaidDays)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSalary = basicSalary.subtract(deductions);

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayPeriodMonth(request.getMonth());
        payroll.setPayPeriodYear(request.getYear());
        payroll.setBasicSalary(basicSalary);
        payroll.setDeductions(deductions);
        payroll.setNetSalary(netSalary);

        return payrollRepository.save(payroll);
    }

    @Transactional
    public PayrollBulkGenerateResponse generatePayrollForAllEmployees(int month, int year) {
        YearMonth requestedPeriod = YearMonth.of(year, month);
        YearMonth currentPeriod = YearMonth.now();
        if (!requestedPeriod.isBefore(currentPeriod)) {
            throw new InvalidRequestException(
                    "Cannot generate payroll for " + month + "/" + year
                            + " — that month hasn't ended yet. Payroll can only be generated for completed months.");
        }

        List<Employee> employees = employeeRepository.findAll();
        int generated = 0;
        int skipped = 0;
        List<String> skippedReasons = new ArrayList<>();

        for (Employee employee : employees) {
            String fullName = employee.getFirstName() + " " + employee.getLastName();

            if (employee.getSalary() == null) {
                skipped++;
                skippedReasons.add(fullName + ": no salary on file");
                continue;
            }

            PayrollGenerateRequest request = new PayrollGenerateRequest();
            request.setEmployeeId(employee.getId());
            request.setMonth(month);
            request.setYear(year);

            try {
                generatePayroll(request);
                generated++;
            } catch (DuplicateResourceException alreadyExists) {
                skipped++;
                skippedReasons.add(fullName + ": already generated for " + month + "/" + year);
            } catch (Exception e) {
                skipped++;
                skippedReasons.add(fullName + ": " + e.getMessage());
                log.error("Failed to generate payroll for employee {}: {}", employee.getId(), e.getMessage());
            }
        }

        return new PayrollBulkGenerateResponse(generated, skipped, skippedReasons);
    }

    private boolean isWeekend(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean isOnApprovedLeave(LocalDate day, List<LeaveRequest> leaves) {
        return leaves.stream().anyMatch(l -> !day.isBefore(l.getStartDate()) && !day.isAfter(l.getEndDate()));
    }

    public List<Payroll> getPayrollByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    public List<Payroll> getAllPayroll() {
        return payrollRepository.findAll();
    }
}