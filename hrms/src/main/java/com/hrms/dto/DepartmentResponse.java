package com.hrms.dto;

import com.hrms.entity.Department;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentResponse {

    private Long id;
    private String name;
    private Long companyId;
    private String companyName;

    public static DepartmentResponse fromEntity(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setCompanyId(department.getCompany().getId());
        response.setCompanyName(department.getCompany().getName());
        return response;
    }
}