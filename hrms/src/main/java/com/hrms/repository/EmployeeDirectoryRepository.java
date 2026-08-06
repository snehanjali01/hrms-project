package com.hrms.repository;

import com.hrms.entity.EmployeeDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeDirectoryRepository extends JpaRepository<EmployeeDirectory, Long> {

    boolean existsByEmployeeId(Long employeeId);
}