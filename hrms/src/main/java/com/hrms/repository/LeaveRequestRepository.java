package com.hrms.repository;

import com.hrms.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import com.hrms.enums.LeaveStatus;


@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeManagerId(Long managerId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    // Any leave that overlaps [from, to], including one that started in an earlier month
    @Query("select l from LeaveRequest l where l.employee.id = :employeeId and l.status = :status "
            + "and l.startDate <= :to and l.endDate >= :from")
    List<LeaveRequest> findOverlapping(@Param("employeeId") Long employeeId,
                                       @Param("status") LeaveStatus status,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);
}