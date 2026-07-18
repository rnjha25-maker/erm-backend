package ermorg.erm.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskReview;

@Repository
public interface RiskReviewRepository extends JpaRepository<RiskReview, Long> {
    
    // Find by risk title
//    List<RiskReview> findByRiskTitleContainingIgnoreCase(String riskTitle);
    
    // Find by status
    List<RiskReview> findByStatus(String status);
    
    // Find by risk rating
    List<RiskReview> findByResidualRiskRating(String riskRating);
    
    // Find by evaluation date range
    List<RiskReview> findByLastEvaluationDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find risks due for evaluation
    List<RiskReview> findByNextEvaluationDateBefore(LocalDate date);
    
    // Custom query to find high-risk items
    @Query("SELECT r FROM RiskReview r WHERE r.residualRiskRating IN ('High', 'Very High')")
    List<RiskReview> findHighRiskItems();
    
//     Find by evaluator
    List<RiskReview> findByRiskEvaluationBy(String evaluator);
    
    // Count by status
    @Query("SELECT COUNT(r) FROM RiskReview r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT r FROM RiskReview r WHERE r.organization.id = :orgId AND r.id = :rieviewId AND r.deleted = false")
	RiskReview getByOrgAndReivewId(@Param("orgId")Long orgId, @Param("rieviewId")Long rieviewId);

    @Query("SELECT r FROM RiskReview r WHERE r.organization.id = :orgId AND r.deleted = false ORDER BY r.id DESC")
	List<RiskReview> getByOrgId(@Param("orgId")Long id);

    @Query("SELECT DISTINCT r FROM RiskReview r LEFT JOIN FETCH r.riskReporting LEFT JOIN FETCH r.risk "
            + "WHERE r.organization.id = :orgId AND r.risk.id IN :riskIds AND r.deleted = false "
            + "AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.id DESC")
    List<RiskReview> findForRiskRegister(@Param("orgId") Long orgId, @Param("riskIds") List<Long> riskIds,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
