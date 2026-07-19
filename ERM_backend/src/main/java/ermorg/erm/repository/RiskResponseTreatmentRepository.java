package ermorg.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskResponseTreatment;

@Repository
public interface RiskResponseTreatmentRepository extends JpaRepository<RiskResponseTreatment, Long> {

	@Query("SELECT r FROM RiskResponseTreatment r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.subRisks LEFT JOIN FETCH r.risk WHERE r.organization.id = :orgId AND r.id = :treatmentId AND r.deleted = false")
	RiskResponseTreatment getOrgRiskResponseTreatment(@Param("orgId") Long orgId, @Param("treatmentId") Long treatmentId);
	
	@Query("SELECT r FROM RiskResponseTreatment r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.subRisks LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	Page<RiskResponseTreatment> getAllOrgRiskResponseTreatments(@Param("organizationId") Long organizationId, Pageable pageable);
	
	@Query("SELECT r FROM RiskResponseTreatment r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.subRisks LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	List<RiskResponseTreatment> getAllOrgRiskResponseTreatmentsNoPage(@Param("organizationId") Long organizationId);

	@Query("SELECT r FROM RiskResponseTreatment r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.subRisks LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	List<RiskResponseTreatment> getAllOrgRiskResponseTreatments(@Param("organizationId") Long organizationId);

	@Query("SELECT DISTINCT r FROM RiskResponseTreatment r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.risk "
			+ "WHERE r.organization.id = :orgId AND r.risk.id IN :riskIds AND r.deleted = false "
			+ "AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.id DESC")
	List<RiskResponseTreatment> findForRiskRegister(@Param("orgId") Long orgId,
			@Param("riskIds") List<Long> riskIds, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate);

}
