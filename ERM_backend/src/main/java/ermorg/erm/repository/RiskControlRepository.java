package ermorg.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskControl;

@Repository
public interface RiskControlRepository extends JpaRepository<RiskControl, Long> {

	@Query("SELECT r FROM RiskControl r LEFT JOIN FETCH r.primaryResponsible LEFT JOIN FETCH r.approver LEFT JOIN FETCH r.subRisk LEFT JOIN FETCH r.risk WHERE r.organization.id = :orgId AND r.id = :controlId AND r.deleted = false")
	RiskControl getOrObject(@Param("orgId")Long orgId, @Param("controlId") Long controlId);

	@Query("SELECT r FROM RiskControl r LEFT JOIN FETCH r.primaryResponsible LEFT JOIN FETCH r.approver LEFT JOIN FETCH r.subRisk LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	Page<RiskControl> getAllRisksByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
	
	@Query("SELECT r FROM RiskControl r LEFT JOIN FETCH r.primaryResponsible LEFT JOIN FETCH r.approver LEFT JOIN FETCH r.subRisk LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	List<RiskControl> getAllRisksByOrganizationIdNoPage(@Param("organizationId") Long organizationId);

	@Query("SELECT r FROM RiskControl r LEFT JOIN FETCH r.primaryResponsible LEFT JOIN FETCH r.approver LEFT JOIN FETCH r.subRisk LEFT JOIN FETCH r.risk WHERE r.organization.id = :organizationId AND r.id = :riskId AND r.deleted = false")
	RiskControl getRisksByOrgIdAndRiskId(@Param("organizationId") Long organizationId, @Param("riskId") Long riskId);

	@Query("SELECT DISTINCT r FROM RiskControl r LEFT JOIN FETCH r.primaryResponsible LEFT JOIN FETCH r.approver "
			+ "LEFT JOIN FETCH r.risk WHERE r.organization.id = :orgId AND r.risk.id IN :riskIds "
			+ "AND r.deleted = false AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.id DESC")
	List<RiskControl> findForRiskRegister(@Param("orgId") Long orgId, @Param("riskIds") List<Long> riskIds,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
