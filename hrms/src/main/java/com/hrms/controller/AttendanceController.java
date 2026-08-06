package com.hrms.controller;

import com.hrms.dto.AttendanceCheckInRequest;
import com.hrms.dto.AttendanceCheckOutRequest;
import com.hrms.dto.AttendanceResponse;
import com.hrms.entity.Attendance;
import com.hrms.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody AttendanceCheckInRequest request) {
        Attendance saved = attendanceService.checkIn(request.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResponse.fromEntity(saved));
    }

    @PutMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(
            @Valid @RequestBody AttendanceCheckOutRequest request) {
        Attendance updated = attendanceService.checkOut(request.getEmployeeId());
        return ResponseEntity.ok(AttendanceResponse.fromEntity(updated));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @RequestParam(required = false) Long employeeId) {

        List<Attendance> records = (employeeId != null)
                ? attendanceService.getAttendanceByEmployee(employeeId)
                : attendanceService.getAllAttendance();

        List<AttendanceResponse> response = records.stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}