package com.org_setup_command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.Branch;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch save(Branch branch);
    
    @Query("SELECT b FROM Branch b WHERE b.company.id = :companyId AND b.deleted != true")
    List<Branch> findBranchesByCompayId(@Param("companyId") Long companyId);
}
