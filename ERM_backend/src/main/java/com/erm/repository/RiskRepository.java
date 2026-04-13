package com.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.Risk;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long>{
	Risk save(Risk risk);
	
	// Paginated methods for better performance with large datasets
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false")
	Page<Risk> getAllRisksByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.id = :riskId AND r.deleted = false")
	Risk getRisksByOrgIdAndRiskId(@Param("organizationId") Long organizationId, @Param("riskId") Long riskId);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdBy.id = :userId AND r.deleted = false")
	Page<Risk> getAllRisksByOrgIdAndCreatedBy(@Param("organizationId") Long organizationId, @Param("userId") Long userId, Pageable pageable);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdBy.id = :userId AND r.createdAt between :startDate AND :endDate AND r.deleted = false")
	Page<Risk> getAllRisksByOrgIdAndCreatedByDateRange(@Param("organizationId") Long organizationId, @Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false")
	Page<Risk> getAllRisksByOrgId(@Param("organizationId") Long organizationId, Pageable pageable);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdAt between :startDate AND :endDate AND r.deleted = false")
	Page<Risk> getAllRisksByOrgIdDateRange(@Param("organizationId") Long organizationId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.companyId = :companyId AND r.createdAt between :startDate AND :endDate AND r.deleted = false")
	Page<Risk> getAllRisksByCompanyDateRange(@Param("companyId") Long companyId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.companyId = :companyId AND r.deleted = false")
	Page<Risk> getAllRisksByCompany(@Param("companyId") Long companyId, Pageable pageable);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdBy.id = :userId AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByOrgIdAndCreatedByNoPage(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdBy.id = :userId AND r.createdAt between :startDate AND :endDate AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByOrgIdAndCreatedByDateRangeNoPage(@Param("organizationId") Long organizationId, @Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByOrgIdNoPage(@Param("organizationId") Long organizationId);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.createdAt between :startDate AND :endDate AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByOrgIdDateRangeNoPage(@Param("organizationId") Long organizationId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.companyId = :companyId AND r.createdAt between :startDate AND :endDate AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByCompanyDateRangeNoPage(@Param("companyId") Long companyId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.companyId = :companyId AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksByCompanyNoPage(@Param("companyId") Long companyId);
	
	// Optimized dashboard queries - fetch critical data upfront
	@Query(value = "SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false ORDER BY r.id DESC", nativeQuery = false)
	List<Risk> getTopRisksByOrganization(@Param("organizationId") Long organizationId, @Param("limit") int limit);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false")
	Page<Risk> getRisksPagedOptimized(@Param("organizationId") Long organizationId, Pageable pageable);
	
	// JOIN FETCH queries to prevent N+1 queries when accessing related entities
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.id = :id AND r.deleted = false")
	Risk getRiskWithOwnerAndChampion(@Param("id") Long id);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false ORDER BY r.id DESC")
	List<Risk> getAllRisksWithOwnerAndChampion(@Param("organizationId") Long organizationId);
	
	@Query("SELECT r FROM Risk r LEFT JOIN FETCH r.riskOwner LEFT JOIN FETCH r.riskChampion LEFT JOIN FETCH r.subRisk WHERE r.organizationId = :organizationId AND r.deleted = false")
	Page<Risk> getAllRisksWithOwnerAndChampionPaged(@Param("organizationId") Long organizationId, Pageable pageable);

}
