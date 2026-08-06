package com.hrms.service;

import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest{

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeCreateRequest request;
    private Company company;
    private Department department;

    @BeforeEach
    void setUp(){
        company = new Company();
        company.setId(1L);

        department = new Department();
        department.setId(1L);

        request = new EmployeeCreateRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test.user@acme.com");
        request.setEmployeeCode("EMP999");
        request.setCompanyId(1L);
        request.setDepartmentId(1L);
        request.setCreateUserAccount(false);
    }

    @Test
    void createEmployee_shouldSaveSuccessfully_whenRequestIsValid() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.findByEmail("test.user@acme.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeCode("EMP999")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.createEmployee(request);

        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getEmail()).isEqualTo("test.user@acme.com");
        verify(employeeRepository).save(any(Employee.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrow_whenCompanyNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrow_whenDepartmentNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void createEmployee_shouldThrow_whenEmailAlreadyExists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.findByEmail("test.user@acme.com"))
                .thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrow_whenEmployeeCodeAlreadyExists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.findByEmail("test.user@acme.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeCode("EMP999"))
                .thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("EMP999");
    }

    @Test
    void getEmployeeById_shouldReturnEmployee_whenExists() {
        Employee employee = new Employee();
        employee.setId(5L);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getEmployeeById_shouldThrow_whenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}