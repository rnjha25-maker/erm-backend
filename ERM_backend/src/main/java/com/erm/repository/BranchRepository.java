package com.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
	
	@Query("select count(b) from Branch b where b.organization.id = :orgId AND b.deleted = false")
	public Long totalBranchesByOrg(@Param("orgId")Long orgId);
	
	@Query("select count(b) from Branch b where b.organization.id = :orgId AND b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.deleted = false")
	public Long totalBranchesByOrg(@Param("orgId")Long orgId, @Param("startDate") Date startDate,@Param("endDate") Date endDate);
	
	@Query("select b from Branch b where b.organization.id = :orgId AND b.company.id = :companyId AND b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.deleted = false")
	public List<Branch> getBranchesByCompany(@Param("orgId")Long orgId, @Param("companyId")Long companyId,@Param("startDate") Date startDate,@Param("endDate") Date endDate);
	
	@Query("select b from Branch b where b.organization.id = :orgId AND b.company.id = :companyId AND b.deleted = false")
	public List<Branch> getBranchesByCompany(@Param("orgId")Long orgId, @Param("companyId")Long companyId);

}
