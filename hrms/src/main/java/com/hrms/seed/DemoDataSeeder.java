package com.hrms.seed;

import com.hrms.dto.CompanyCreateRequest;
import com.hrms.dto.DepartmentCreateRequest;
import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.User;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Creates one demo user per role so the app can be logged into on a fresh database.
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
        seedEmployee("Admin", "User", "admin", "EMP001", company, department, null, "ADMIN", 120000);
        seedEmployee("HR", "User", "hr", "EMP002", company, department, null, "HR", 80000);
        seedEmployee("Employee", "User", "employee", "EMP004", company, department, manager.getId(), DEFAULT_ROLE, 60000);

        log.info("Demo data seeded: users admin, hr, manager, employee (password {})", DEMO_PASSWORD);
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
