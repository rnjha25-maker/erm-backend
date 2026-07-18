package ermorg.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.KriKpiReview;

@Repository
public interface KriKpiRiskRepository extends JpaRepository<KriKpiReview, Long> {

	@Query("SELECT k FROM KriKpiReview k LEFT JOIN FETCH k.riskAssessment WHERE k.organization.id = :orgId AND k.id = :kriId AND k.deleted = false")
	KriKpiReview getByOrgIdAndKriId(@Param("orgId") Long id, @Param("kriId") Long kriId);

	@Query("SELECT k FROM KriKpiReview k LEFT JOIN FETCH k.riskAssessment WHERE k.organization.id = :orgId AND k.deleted = false ORDER BY k.id DESC")
	List<KriKpiReview> getByOrgId(@Param("orgId") Long orgId);

	@Query("SELECT DISTINCT k FROM KriKpiReview k LEFT JOIN FETCH k.riskAssessment LEFT JOIN FETCH k.risk "
			+ "WHERE k.organization.id = :orgId AND k.risk.id IN :riskIds AND k.deleted = false "
			+ "AND k.createdAt BETWEEN :startDate AND :endDate ORDER BY k.id DESC")
	List<KriKpiReview> findForRiskRegister(@Param("orgId") Long orgId, @Param("riskIds") List<Long> riskIds,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
