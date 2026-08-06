package com.hrms.service;

import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import com.hrms.enums.AttendanceStatus;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Attendance checkIn(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Employee " + employeeId + " has already checked in today");
                });

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(ZonedDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new InvalidRequestException(
                        "Employee " + employeeId + " has not checked in today"));

        if (attendance.getCheckOutTime() != null) {
            throw new InvalidRequestException(
                    "Employee " + employeeId + " has already checked out today");
        }

        attendance.setCheckOutTime(ZonedDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
}