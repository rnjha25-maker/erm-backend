package ermorg.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.ERMMaturityAssessment;

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

	@Query("SELECT COUNT(m) FROM ERMMaturityAssessment m WHERE m.organization.id = :orgId AND m.ermMaturityId = :ermMaturityId AND m.deleted = false")
	long countByOrganizationIdAndErmMaturityId(@Param("orgId") Long orgId, @Param("ermMaturityId") String ermMaturityId);

	@Query("SELECT DISTINCT m FROM ERMMaturityAssessment m LEFT JOIN FETCH m.company "
			+ "WHERE m.organization.id = :orgId AND m.deleted = false "
			+ "AND m.ermMaturityId IS NOT NULL "
			+ "ORDER BY m.ermMaturityId")
	List<ERMMaturityAssessment> findAllGroupedByOrg(@Param("orgId") Long orgId);

	@Query("SELECT DISTINCT m FROM ERMMaturityAssessment m LEFT JOIN FETCH m.company "
			+ "WHERE m.organization.id = :orgId AND m.deleted = false "
			+ "AND m.ermMaturityId IS NOT NULL "
			+ "AND COALESCE(m.lastAssessmentDate, m.createdAt) BETWEEN :startDate AND :endDate "
			+ "AND (:scopeCompanyId IS NULL OR m.company.id = :scopeCompanyId) "
			+ "AND (:functionId IS NULL OR :functionId = 0 OR (:functionId <> 0 AND :functionId MEMBER OF m.departmentIds)) "
			+ "ORDER BY m.ermMaturityId")
	List<ERMMaturityAssessment> findForErmDashboard(@Param("orgId") Long orgId, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("scopeCompanyId") Long scopeCompanyId,
			@Param("functionId") Long functionId);

	@Query("SELECT DISTINCT m FROM ERMMaturityAssessment m LEFT JOIN FETCH m.company "
			+ "WHERE m.organization.id = :orgId AND m.company.id IN :companyIds AND m.deleted = false "
			+ "AND m.createdAt BETWEEN :startDate AND :endDate "
			+ "AND (:functionId IS NULL OR :functionId = 0 OR :functionId MEMBER OF m.departmentIds) "
			+ "ORDER BY m.id DESC")
	List<ERMMaturityAssessment> findForRiskRegister(@Param("orgId") Long orgId,
			@Param("companyIds") List<Long> companyIds, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("functionId") Long functionId);

}
