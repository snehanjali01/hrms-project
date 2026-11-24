package com.hrms.service;

import com.hrms.dto.PayrollGenerateRequest;
import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.Payroll;
import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.LeaveStatus;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.InvalidRequestException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Uses January 2024: 31 days, starts on a Monday, 23 weekdays and 8 weekend days.
 * Salary 23000 over 23 working days makes the per-day rate a clean 1000.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    private static final int MONTH = 1;
    private static final int YEAR = 2024;
    private static final LocalDate START = LocalDate.of(YEAR, MONTH, 1);
    private static final LocalDate END = LocalDate.of(YEAR, MONTH, 31);

    @Mock private PayrollRepository payrollRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks private PayrollService payrollService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setSalary(new BigDecimal("23000.00"));
    }

    // ---------- helpers ----------

    private PayrollGenerateRequest request(int month, int year) {
        PayrollGenerateRequest r = new PayrollGenerateRequest();
        r.setEmployeeId(1L);
        r.setMonth(month);
        r.setYear(year);
        return r;
    }

    private void givenEmployeeExistsAndNoPayrollYet() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenAttendance(List<Attendance> records) {
        when(attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(1L, START, END)).thenReturn(records);
    }

    private void givenApprovedLeaves(List<LeaveRequest> leaves) {
        when(leaveRequestRepository.findOverlapping(
                eq(1L), eq(LeaveStatus.APPROVED), eq(START), eq(END))).thenReturn(leaves);
    }

    private Attendance attendance(LocalDate date, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setAttendanceDate(date);
        a.setStatus(status);
        return a;
    }

    private List<Attendance> presentOn(Stream<LocalDate> dates) {
        List<Attendance> out = new ArrayList<>();
        dates.forEach(d -> out.add(attendance(d, AttendanceStatus.PRESENT)));
        return out;
    }

    private Stream<LocalDate> allDays() {
        return START.datesUntil(END.plusDays(1));
    }

    private Stream<LocalDate> weekdays() {
        return allDays().filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY);
    }

    private LeaveRequest leave(LocalDate from, LocalDate to) {
        LeaveRequest l = new LeaveRequest();
        l.setStartDate(from);
        l.setEndDate(to);
        l.setStatus(LeaveStatus.APPROVED);
        return l;
    }

    // ---------- guards ----------

    @Test
    void rejectsCurrentMonth() {
        YearMonth now = YearMonth.now();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.generatePayroll(request(now.getMonthValue(), now.getYear())))
                .isInstanceOf(InvalidRequestException.class);
        verify(payrollRepository, never()).save(any());
    }

    @Test
    void rejectsFutureMonth() {
        YearMonth next = YearMonth.now().plusMonths(1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.generatePayroll(request(next.getMonthValue(), next.getYear())))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void rejectsDuplicatePayroll() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(1L, MONTH, YEAR))
                .thenReturn(Optional.of(new Payroll()));

        assertThatThrownBy(() -> payrollService.generatePayroll(request(MONTH, YEAR)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(payrollRepository, never()).save(any());
    }

    @Test
    void rejectsEmployeeWithoutSalary() {
        employee.setSalary(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.findByEmployeeIdAndPayPeriodMonthAndPayPeriodYear(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        givenAttendance(List.of());
        givenApprovedLeaves(List.of());

        assertThatThrownBy(() -> payrollService.generatePayroll(request(MONTH, YEAR)))
                .isInstanceOf(InvalidRequestException.class);
    }

    // ---------- calculation ----------

    @Test
    void noDeductionWhenPresentEveryDay() {
        givenEmployeeExistsAndNoPayrollYet();
        givenAttendance(presentOn(allDays()));
        givenApprovedLeaves(List.of());

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
        assertThat(p.getNetSalary()).isEqualByComparingTo("23000.00");
    }

    @Test
    void deductsOneDayRatePerUnpaidWorkingDay() {
        givenEmployeeExistsAndNoPayrollYet();
        // present on every day except Jan 8-10 (Mon-Wed)
        givenAttendance(presentOn(allDays().filter(d -> d.getDayOfMonth() < 8 || d.getDayOfMonth() > 10)));
        givenApprovedLeaves(List.of());

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("3000.00");
        assertThat(p.getNetSalary()).isEqualByComparingTo("20000.00");
    }

    @Test
    void approvedLeaveIsNotDeducted() {
        givenEmployeeExistsAndNoPayrollYet();
        // present every day except Jan 8-10, which are covered by approved leave
        givenAttendance(presentOn(allDays().filter(d -> d.getDayOfMonth() < 8 || d.getDayOfMonth() > 10)));
        givenApprovedLeaves(List.of(leave(LocalDate.of(YEAR, 1, 8), LocalDate.of(YEAR, 1, 10))));

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
    }

    @Test
    void leaveRunningPastMonthEndOnlyCountsDaysInsideTheMonth() {
        givenEmployeeExistsAndNoPayrollYet();
        // present every day except Jan 30-31; leave is Jan 30 - Feb 3, only 2 days belong to January
        givenAttendance(presentOn(allDays().filter(d -> d.getDayOfMonth() < 30)));
        givenApprovedLeaves(List.of(leave(LocalDate.of(YEAR, 1, 30), LocalDate.of(YEAR, 2, 3))));

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
    }

    // ---------- regression tests for bugs found in the first version ----------

    @Test
    void weekendsAreNotDeductedWhenEmployeeWorkedEveryWeekday() {
        givenEmployeeExistsAndNoPayrollYet();
        givenAttendance(presentOn(weekdays()));
        givenApprovedLeaves(List.of());

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
    }

    @Test
    void leaveThatStartedLastMonthCoversTheFirstDaysOfThisMonth() {
        givenEmployeeExistsAndNoPayrollYet();
        // leave Dec 28 - Jan 3 covers Mon-Wed Jan 1-3; present every other weekday
        givenAttendance(presentOn(weekdays().filter(d -> d.getDayOfMonth() > 3)));
        givenApprovedLeaves(List.of(leave(LocalDate.of(2023, 12, 28), LocalDate.of(YEAR, 1, 3))));

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
    }

    @Test
    void leaveOverAWeekendOnlyCoversTheWeekdays() {
        givenEmployeeExistsAndNoPayrollYet();
        // leave Fri Jan 5 - Mon Jan 8: only the Friday and the Monday are working days
        givenAttendance(presentOn(weekdays().filter(d -> d.getDayOfMonth() != 5 && d.getDayOfMonth() != 8)));
        givenApprovedLeaves(List.of(leave(LocalDate.of(YEAR, 1, 5), LocalDate.of(YEAR, 1, 8))));

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("0.00");
    }

    @Test
    void halfDayDeductsHalfADayRate() {
        givenEmployeeExistsAndNoPayrollYet();
        List<Attendance> records = presentOn(allDays().filter(d -> d.getDayOfMonth() != 8));
        records.add(attendance(LocalDate.of(YEAR, 1, 8), AttendanceStatus.HALF_DAY));
        givenAttendance(records);
        givenApprovedLeaves(List.of());

        Payroll p = payrollService.generatePayroll(request(MONTH, YEAR));

        assertThat(p.getDeductions()).isEqualByComparingTo("500.00");
    }
}
