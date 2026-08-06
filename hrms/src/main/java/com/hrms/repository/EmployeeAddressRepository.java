package com.hrms.repository;

import com.hrms.entity.EmployeeAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeAddressRepository extends JpaRepository<EmployeeAddress, Long> {

    Optional<EmployeeAddress> findByEmployeeId(Long employeeId);
}