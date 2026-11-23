package com.hrms.service;

import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.dto.EmployeeEditRequest;
import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.Role;
import com.hrms.entity.User;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.RoleRepository;
import com.hrms.repository.UserRepository;
import com.hrms.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private static final String DEFAULT_ROLE = "EMPLOYEE";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + request.getCompanyId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + request.getManagerId()));

            if (!manager.getCompany().getId().equals(company.getId())) {
                throw new InvalidRequestException(
                        "Manager must belong to the same company as the employee");
            }
        }

        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Employee with email '" + request.getEmail() + "' already exists");
        }

        if (employeeRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new DuplicateResourceException(
                    "Employee with code '" + request.getEmployeeCode() + "' already exists");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setCompany(company);
        employee.setDepartment(department);
        employee.setManager(manager);

        Employee savedEmployee = employeeRepository.save(employee);

        if (request.isCreateUserAccount()) {

            if (request.getUsername() == null || request.getPassword() == null) {
                throw new InvalidRequestException(
                        "Username and password are required when createUserAccount is true");
            }

            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new DuplicateResourceException(
                        "Username '" + request.getUsername() + "' already exists");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmployee(savedEmployee);
            Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                    .orElseThrow(() -> new IllegalStateException("Role " + DEFAULT_ROLE + " is not seeded"));
            user.setRoles(new HashSet<>(Set.of(defaultRole)));

            userRepository.save(user);
        }

        return savedEmployee;
    }

    @Transactional
    public Employee editEmployee(Long id, EmployeeEditRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + request.getCompanyId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new InvalidRequestException("An employee cannot be their own manager");
            }
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + request.getManagerId()));

            if (!manager.getCompany().getId().equals(company.getId())) {
                throw new InvalidRequestException(
                        "Manager must belong to the same company as the employee");
            }
        }

        employeeRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Employee with email '" + request.getEmail() + "' already exists");
                });

        employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Employee with code '" + request.getEmployeeCode() + "' already exists");
                });

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setCompany(company);
        employee.setDepartment(department);
        employee.setManager(manager);

        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));
    }

    public Page<Employee> searchEmployees(
            String firstName,
            String lastName,
            String email,
            String employeeCode,
            Long companyId,
            Long departmentId,
            Long managerId,
            Pageable pageable) {

        Specification<Employee> spec = EmployeeSpecification.filterBy(
                firstName, lastName, email, employeeCode, companyId, departmentId, managerId
        );

        return employeeRepository.findAll(spec, pageable);
    }
}