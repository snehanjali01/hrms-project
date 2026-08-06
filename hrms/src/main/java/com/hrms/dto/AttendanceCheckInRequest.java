package com.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceCheckInRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}