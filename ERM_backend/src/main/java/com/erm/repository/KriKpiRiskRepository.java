package com.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.KriKpiReview;

@Repository
public interface KriKpiRiskRepository extends JpaRepository<KriKpiReview, Long> {

	@Query("SELECT k FROM KriKpiReview k WHERE k.organization.id = :orgId AND k.id = :kriId AND k.deleted = false")
	KriKpiReview getByOrgIdAndKriId(@Param("orgId") Long id, @Param("kriId") Long kriId);

	@Query("SELECT k FROM KriKpiReview k WHERE k.organization.id = :orgId AND k.deleted = false")
	List<KriKpiReview> getByOrgId(@Param("orgId") Long orgId);

}
