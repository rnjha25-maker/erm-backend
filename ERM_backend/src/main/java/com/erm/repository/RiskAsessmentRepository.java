package com.erm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.RiskAssessment;

@Repository
public interface RiskAsessmentRepository extends JpaRepository<RiskAssessment, Long> {

	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH r.subRisks WHERE r.organization.id = :orgId AND r.id = :assessmentId")
	RiskAssessment getAssessmentByOrgIdAndAssessmentId(@Param("orgId")Long orgId, @Param("assessmentId")Long assessmentId);

	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH r.subRisks WHERE r.organization.id = :orgId AND r.deleted = false")
	Page<RiskAssessment> getAllByOrgId(@Param("orgId")Long orgId, Pageable pageable);
	
	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH r.subRisks WHERE r.organization.id = :orgId AND r.deleted = false")
	List<RiskAssessment> getAllByOrgIdNoPage(@Param("orgId")Long orgId);

}
