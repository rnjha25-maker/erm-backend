package com.erm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.ERMMaturityAssessment;

@Repository
public interface ErmMaturityRepository extends JpaRepository<ERMMaturityAssessment, Long> {

	@Query("Select m from ERMMaturityAssessment m where m.organization.id = :orgId AND m.id = :maturityId AND m.deleted = false")
	public ERMMaturityAssessment getByOrg(@Param("orgId")Long orgId,@Param("maturityId") Long maturityId);

	@Query("Select m from ERMMaturityAssessment m where m.organization.id = :orgId AND m.deleted = false")
	public Page<ERMMaturityAssessment> getAllByOrg(@Param("orgId")Long orgId, Pageable pageable);
	
	@Query("Select m from ERMMaturityAssessment m where m.organization.id = :orgId AND m.deleted = false")
	public List<ERMMaturityAssessment> getAllByOrgNoPage(@Param("orgId")Long orgId);

	@Query("Select m from ERMMaturityAssessment m where m.organization.id = :orgId AND m.id in (:maturityIds) AND m.deleted = false")
	public List<ERMMaturityAssessment> getAllByOrgAndMaturityIds(@Param("orgId")Long orgId, @Param("maturityIds") List<Long> maturityIds);
	
	@Query("Select m from ERMMaturityAssessment m where m.organization.id = :orgId AND m.id in (:maturityIds) AND m.deleted = false")
	public Page<ERMMaturityAssessment> getAllByOrgAndMaturityIdsPageable(@Param("orgId")Long orgId, @Param("maturityIds") List<Long> maturityIds, Pageable pageable);

}
