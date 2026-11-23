package com.hrms.controller;

import com.hrms.dto.LeaveApprovalRequest;
import com.hrms.dto.LeaveRequestCreateRequest;
import com.hrms.dto.LeaveRequestResponse;
import com.hrms.entity.LeaveRequest;
import com.hrms.security.AccessRules;
import com.hrms.security.AuthenticatedUser;
import com.hrms.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController{

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        // The requester is always the logged-in user, never an id sent by the client
        request.setEmployeeId(caller.employeeId());
        LeaveRequest saved = leaveRequestService.submitLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveRequestResponse.fromEntity(saved));
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<LeaveRequestResponse> decideLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        // The approver is always the logged-in user, never an id sent by the client
        request.setApproverId(caller.employeeId());
        LeaveRequest updated = leaveRequestService.processDecision(id, request);
        return ResponseEntity.ok(LeaveRequestResponse.fromEntity(updated));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequests(
            @RequestParam(required = false) Long employeeId,
            Authentication authentication,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        Long scopedEmployeeId = AccessRules.scopeToCaller(authentication, caller, employeeId);

        List<LeaveRequest> leaveRequests = (scopedEmployeeId != null)
                ? leaveRequestService.getLeaveRequestsByEmployee(scopedEmployeeId)
                : leaveRequestService.getAllLeaveRequests();

        List<LeaveRequestResponse> response = leaveRequests.stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestResponse>> getPendingApprovals(
            @AuthenticationPrincipal AuthenticatedUser caller) {

        List<LeaveRequestResponse> response = leaveRequestService
                .getPendingApprovalsForManager(caller.employeeId()).stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}
