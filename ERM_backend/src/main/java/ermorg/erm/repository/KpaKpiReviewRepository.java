package ermorg.erm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.KpaKpiReview;

@Repository
public interface KpaKpiReviewRepository extends JpaRepository<KpaKpiReview, Long> {

    @Query("""
            SELECT k FROM KpaKpiReview k
            WHERE k.organization.id = :orgId
              AND k.company.id = :companyId
              AND k.id = :reviewId
              AND k.deleted = false
            """)
    KpaKpiReview getByOrgIdAndCompanyIdAndReviewId(
            @Param("orgId") Long orgId,
            @Param("companyId") Long companyId,
            @Param("reviewId") Long reviewId);

    @Query("""
            SELECT k FROM KpaKpiReview k
            WHERE k.organization.id = :orgId
              AND k.company.id = :companyId
              AND k.deleted = false
              AND (:status IS NULL OR k.status = :status)
              AND (:search IS NULL
                   OR LOWER(k.kpa) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(k.keyPerformanceIndicator) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY k.id DESC
            """)
    Page<KpaKpiReview> getByOrgIdAndCompanyId(
            @Param("orgId") Long orgId,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);
}
