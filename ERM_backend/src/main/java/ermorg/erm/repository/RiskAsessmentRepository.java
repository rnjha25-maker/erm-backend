package ermorg.erm.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskAssessment;

@Repository
public interface RiskAsessmentRepository extends JpaRepository<RiskAssessment, Long> {

	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk WHERE r.id = :id AND r.organization.id = :orgId AND r.deleted = false")
	Optional<RiskAssessment> findByIdAndOrganizationIdAndDeletedFalse(@Param("id") Long id, @Param("orgId") Long organizationId);

	@Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH "
			+ "r.subRisk WHERE r.organization.id = :orgId AND r.id = :assessmentId AND r.deleted = false")
	RiskAssessment getAssessmentByOrgIdAndAssessmentId(@Param("orgId") Long orgId,
			@Param("assessmentId") Long assessmentId);

	@Query("SELECT r.id FROM RiskAssessment r WHERE r.organization.id = :orgId AND r.deleted = false ORDER BY r.id DESC")
	Page<Long> getAllIdsByOrgId(@Param("orgId") Long orgId, Pageable pageable);

	@Query("SELECT DISTINCT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN "
			+ "FETCH r.subRisk WHERE r.organization.id = :orgId AND r.id IN :assessmentIds AND r.deleted = false")
	List<RiskAssessment> getAllByOrgIdAndIds(@Param("orgId") Long orgId,
			@Param("assessmentIds") List<Long> assessmentIds);

	@Query("SELECT DISTINCT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT "
			+ "JOIN FETCH r.subRisk WHERE r.organization.id = :orgId AND r.deleted = false ORDER BY r.id DESC")
	List<RiskAssessment> getAllByOrgIdNoPage(@Param("orgId") Long orgId);

	@Query("SELECT DISTINCT r FROM RiskAssessment r LEFT JOIN FETCH r.risk LEFT JOIN FETCH r.subRisk "
			+ "WHERE r.organization.id = :orgId AND r.risk.id IN :riskIds AND r.deleted = false "
			+ "AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.id DESC")
	List<RiskAssessment> findForRiskRegister(@Param("orgId") Long orgId, @Param("riskIds") List<Long> riskIds,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
