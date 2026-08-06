package com.hrms.dto;

import com.hrms.entity.LeaveRequest;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import com.hrms.enums.LeaveStatus;

@Getter
@Setter
public class LeaveRequestResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private Long approvedById;
    private String approvedByName;

    public static LeaveRequestResponse fromEntity(LeaveRequest leaveRequest) {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(leaveRequest.getId());
        response.setEmployeeId(leaveRequest.getEmployee().getId());
        response.setEmployeeName(
                leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
        response.setStartDate(leaveRequest.getStartDate());
        response.setEndDate(leaveRequest.getEndDate());
        response.setReason(leaveRequest.getReason());
        response.setStatus(leaveRequest.getStatus());

        if (leaveRequest.getApprovedBy() != null) {
            response.setApprovedById(leaveRequest.getApprovedBy().getId());
            response.setApprovedByName(
                    leaveRequest.getApprovedBy().getFirstName() + " " + leaveRequest.getApprovedBy().getLastName());
        }

        return response;
    }
}