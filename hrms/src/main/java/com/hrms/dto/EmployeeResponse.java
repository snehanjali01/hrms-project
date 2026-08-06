package com.hrms.dto;

import com.hrms.entity.Employee;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeCode;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerName;
    private BigDecimal salary;
    private LocalDateTime joiningDate;

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public static EmployeeResponse fromEntity(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setCompanyId(employee.getCompany().getId());
        response.setCompanyName(employee.getCompany().getName());
        response.setDepartmentId(employee.getDepartment().getId());
        response.setDepartmentName(employee.getDepartment().getName());
        response.setSalary(employee.getSalary());
        response.setJoiningDate(employee.getCreatedAt());

        if (employee.getManager() != null) {
            response.setManagerId(employee.getManager().getId());
            response.setManagerName(
                    employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }

        if (employee.getAddress() != null) {
            response.setStreet(employee.getAddress().getStreet());
            response.setCity(employee.getAddress().getCity());
            response.setState(employee.getAddress().getState());
            response.setPostalCode(employee.getAddress().getPostalCode());
            response.setCountry(employee.getAddress().getCountry());
        }

        return response;
    }
}