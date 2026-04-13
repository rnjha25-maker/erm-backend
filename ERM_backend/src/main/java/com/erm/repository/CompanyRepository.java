package com.erm.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
	
	@Query("select count(c) from Company c where c.organization.id = :orgId AND c.deleted = false")
	public Long totalCompanies(@Param("orgId") Long orgId);
	
	@Query("select count(c) from Company c where c.organization.id = :orgId AND c.createdAt >= :startDate AND c.createdAt <= :endDate AND c.deleted = false")
	public Long totalCompanies(@Param("orgId") Long orgId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
	
	// JOIN FETCH queries to prevent N+1 queries when accessing related entities
	@Query("SELECT c FROM Company c JOIN FETCH c.branches WHERE c.id = :id AND c.deleted = false")
	Company getCompanyWithBranches(@Param("id") Long id);

}
