package com.hrms.service;

import com.hrms.dto.LeaveApprovalRequest;
import com.hrms.dto.LeaveRequestCreateRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveRequest;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.hrms.enums.LeaveStatus;

@Service
@RequiredArgsConstructor
public class LeaveRequestService{

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequest submitLeaveRequest(LeaveRequestCreateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidRequestException("End date cannot be before start date");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());

        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest processDecision(Long leaveRequestId, LeaveApprovalRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found with id: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidRequestException(
                    "This leave request has already been " + leaveRequest.getStatus().name().toLowerCase());
        }

        Employee approver = employeeRepository.findById(request.getApproverId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approver not found with id: " + request.getApproverId()));

        Employee requester = leaveRequest.getEmployee();
        Employee requesterManager = requester.getManager();

        if (requesterManager == null || !requesterManager.getId().equals(approver.getId())) {
            throw new InvalidRequestException(
                    "Only the employee's manager can approve or reject this leave request");
        }

        LeaveStatus decision;
        try {
            decision = LeaveStatus.valueOf(request.getDecision().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Decision must be either APPROVED or REJECTED");
        }
        if (decision == LeaveStatus.PENDING) {
            throw new InvalidRequestException("Decision must be either APPROVED or REJECTED");
        }

        leaveRequest.setStatus(decision);
        leaveRequest.setStatus(decision);
        leaveRequest.setApprovedBy(approver);

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }
    public List<LeaveRequest> getPendingApprovalsForManager(Long managerId) {
        return leaveRequestRepository.findByEmployeeManagerId(managerId).stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.PENDING)
                .toList();
    }
}