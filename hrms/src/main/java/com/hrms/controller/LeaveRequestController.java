package com.hrms.controller;

import com.hrms.dto.LeaveApprovalRequest;
import com.hrms.dto.LeaveRequestCreateRequest;
import com.hrms.dto.LeaveRequestResponse;
import com.hrms.entity.LeaveRequest;
import com.hrms.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController{

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        LeaveRequest saved = leaveRequestService.submitLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveRequestResponse.fromEntity(saved));
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<LeaveRequestResponse> decideLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request) {
        LeaveRequest updated = leaveRequestService.processDecision(id, request);
        return ResponseEntity.ok(LeaveRequestResponse.fromEntity(updated));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequests(
            @RequestParam(required = false) Long employeeId) {

        List<LeaveRequest> leaveRequests = (employeeId != null)
                ? leaveRequestService.getLeaveRequestsByEmployee(employeeId)
                : leaveRequestService.getAllLeaveRequests();

        List<LeaveRequestResponse> response = leaveRequests.stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestResponse>> getPendingApprovals(
            @RequestParam Long managerId) {

        List<LeaveRequestResponse> response = leaveRequestService
                .getPendingApprovalsForManager(managerId).stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}