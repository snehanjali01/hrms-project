package com.hrms.dto;

import com.hrms.entity.Attendance;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import com.hrms.enums.AttendanceStatus;

@Getter
@Setter
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate attendanceDate;
    private ZonedDateTime checkInTime;
    private ZonedDateTime checkOutTime;
    private AttendanceStatus status;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(attendance.getId());
        response.setEmployeeId(attendance.getEmployee().getId());
        response.setEmployeeName(
                attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setStatus(attendance.getStatus());
        return response;
    }
}