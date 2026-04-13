package com.org_setup_command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.Department;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department save(Department department);

    @Query("SELECT d FROM Department d WHERE d.branch.id = :branchId AND d.deleted != true")
	List<Department> getDepartmentsByBranchId(@Param("branchId") Long branchId);
}
