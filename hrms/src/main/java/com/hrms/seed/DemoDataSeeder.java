package com.hrms.seed;

import com.hrms.dto.CompanyCreateRequest;
import com.hrms.dto.DepartmentCreateRequest;
import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.entity.Attendance;
import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.User;
import com.hrms.enums.AttendanceStatus;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.RoleRepository;
import com.hrms.repository.UserRepository;
import com.hrms.service.CompanyService;
import com.hrms.service.DepartmentService;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates one demo user per role, plus last month of attendance, so the app can be logged into on a fresh database.
 * Off by default; enable with APP_SEED_DEMO_DATA=true. Does nothing if any user already exists.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    static final String DEMO_PASSWORD = "Demo@12345";
    private static final String DEFAULT_ROLE = "EMPLOYEE";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AttendanceRepository attendanceRepository;
    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Demo data not seeded: users already exist");
            return;
        }

        CompanyCreateRequest companyRequest = new CompanyCreateRequest();
        companyRequest.setName("Acme Corp");
        Company company = companyService.createCompany(companyRequest);

        DepartmentCreateRequest departmentRequest = new DepartmentCreateRequest();
        departmentRequest.setName("Engineering");
        departmentRequest.setCompanyId(company.getId());
        Department department = departmentService.createDepartment(departmentRequest);

        Employee manager = seedEmployee("Manager", "User", "manager", "EMP003", company, department, null, "MANAGER", 90000);
        Employee admin = seedEmployee("Admin", "User", "admin", "EMP001", company, department, null, "ADMIN", 120000);
        Employee hr = seedEmployee("HR", "User", "hr", "EMP002", company, department, null, "HR", 80000);
        Employee employee = seedEmployee("Employee", "User", "employee", "EMP004", company, department, manager.getId(), DEFAULT_ROLE, 60000);

        // Last month of attendance so the payroll demo shows real numbers.
        // Employee misses two working days and works one half day; everyone else is present every day.
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        List<LocalDate> workingDays = workingDays(lastMonth);
        for (Employee present : List.of(admin, hr, manager)) {
            seedAttendance(present, workingDays, List.of(), List.of());
        }
        seedAttendance(employee, workingDays, List.of(workingDays.get(2), workingDays.get(3)), List.of(workingDays.get(9)));

        log.info("Demo data seeded: users admin, hr, manager, employee (password {})", DEMO_PASSWORD);
    }

    static List<LocalDate> workingDays(YearMonth month) {
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate day = month.atDay(1); !day.isAfter(month.atEndOfMonth()); day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days.add(day);
            }
        }
        return days;
    }

    private void seedAttendance(Employee employee, List<LocalDate> workingDays,
                                List<LocalDate> absentDays, List<LocalDate> halfDays) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        for (LocalDate day : workingDays) {
            if (absentDays.contains(day)) {
                continue;
            }
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setAttendanceDate(day);
            attendance.setCheckInTime(ZonedDateTime.of(day, LocalTime.of(9, 30), zone));
            boolean half = halfDays.contains(day);
            attendance.setCheckOutTime(ZonedDateTime.of(day, half ? LocalTime.of(13, 30) : LocalTime.of(18, 0), zone));
            attendance.setStatus(half ? AttendanceStatus.HALF_DAY : AttendanceStatus.PRESENT);
            attendanceRepository.save(attendance);
        }
    }

    private Employee seedEmployee(String firstName, String lastName, String username, String code,
                                  Company company, Department department, Long managerId,
                                  String role, int salary) {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(username + "@demo.hrms");
        request.setEmployeeCode(code);
        request.setCompanyId(company.getId());
        request.setDepartmentId(department.getId());
        request.setManagerId(managerId);
        request.setCreateUserAccount(true);
        request.setUsername(username);
        request.setPassword(DEMO_PASSWORD);

        Employee employee = employeeService.createEmployee(request);
        employee.setSalary(BigDecimal.valueOf(salary));

        if (!DEFAULT_ROLE.equals(role)) {
            User user = userRepository.findByEmployeeId(employee.getId()).orElseThrow();
            user.setRoles(new HashSet<>(Set.of(roleRepository.findByName(role).orElseThrow())));
        }
        return employee;
    }
}
