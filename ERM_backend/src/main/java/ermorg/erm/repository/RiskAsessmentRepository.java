package ermorg.erm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskAssessment;

@Repository
public interface RiskAsessmentRepository extends JpaRepository<RiskAssessment, Long> {

	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH "
			+ "r.subRisk WHERE r.organization.id = :orgId AND r.id = :assessmentId AND r.deleted = false")
	RiskAssessment getAssessmentByOrgIdAndAssessmentId(@Param("orgId") Long orgId,
			@Param("assessmentId") Long assessmentId);

	@Query("SELECT r.id FROM RiskAssessment r WHERE r.organization.id = :orgId AND r.deleted = false")
	Page<Long> getAllIdsByOrgId(@Param("orgId") Long orgId, Pageable pageable);

	@Query("SELECT DISTINCT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN "
			+ "FETCH r.subRisk WHERE r.organization.id = :orgId AND r.id IN :assessmentIds AND r.deleted = false")
	List<RiskAssessment> getAllByOrgIdAndIds(@Param("orgId") Long orgId,
			@Param("assessmentIds") List<Long> assessmentIds);

	@Query("SELECT DISTINCT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT "
			+ "JOIN FETCH r.subRisk WHERE r.organization.id = :orgId AND r.deleted = false")
	List<RiskAssessment> getAllByOrgIdNoPage(@Param("orgId") Long orgId);

}
