package com.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveApprovalRequest {

    // Set from the logged-in user's token by the controller; any value sent by the client is ignored
    private Long approverId;

    @NotNull(message = "Decision is required")
    private String decision;
}