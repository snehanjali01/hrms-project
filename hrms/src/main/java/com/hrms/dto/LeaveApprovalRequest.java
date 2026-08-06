package com.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveApprovalRequest {

    @NotNull(message = "Approver ID is required")
    private Long approverId;

    @NotNull(message = "Decision is required")
    private String decision;
}