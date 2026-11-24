package com.hrms.controller;

import com.hrms.dto.AttendanceResponse;
import com.hrms.entity.Attendance;
import com.hrms.security.AccessRules;
import com.hrms.security.AuthenticatedUser;
import com.hrms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(@AuthenticationPrincipal AuthenticatedUser caller) {
        Attendance saved = attendanceService.checkIn(caller.employeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResponse.fromEntity(saved));
    }

    @PutMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(@AuthenticationPrincipal AuthenticatedUser caller) {
        Attendance updated = attendanceService.checkOut(caller.employeeId());
        return ResponseEntity.ok(AttendanceResponse.fromEntity(updated));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @RequestParam(required = false) Long employeeId,
            Authentication authentication,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        Long scopedEmployeeId = AccessRules.scopeToCaller(authentication, caller, employeeId);

        List<Attendance> records = (scopedEmployeeId != null)
                ? attendanceService.getAttendanceByEmployee(scopedEmployeeId)
                : attendanceService.getAllAttendance();

        List<AttendanceResponse> response = records.stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}
