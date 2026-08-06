package com.hrms.controller;

import com.hrms.dto.DepartmentCreateRequest;
import com.hrms.dto.DepartmentResponse;
import com.hrms.entity.Department;
import com.hrms.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        Department saved = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DepartmentResponse.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(
            @RequestParam(required = false) Long companyId) {

        List<Department> departments = (companyId != null)
                ? departmentService.getDepartmentsByCompany(companyId)
                : departmentService.getAllDepartments();

        List<DepartmentResponse> response = departments.stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(DepartmentResponse.fromEntity(department));
    }
}