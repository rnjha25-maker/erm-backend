package ermorg.erm.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.Escalation;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    
    // Find by priority
    List<Escalation> findByPriority(String priority);
    
    // Find by status
    List<Escalation> findByStatus(String status);
    
    // Find by escalation required
    List<Escalation> findByEscalationRequired(String escalationRequired);
    
    // Find by escalation email id
    List<Escalation> findByEscalationEmailId(String escalationEmailId);
    
    // Find by assigned to primary responsible
    List<Escalation> findByAssignedToPrimaryResponsible(String assignedToPrimaryResponsible);
    
    // Find by reporting level
//    List<Escalation> findByReportingLevel(String reportingLevel);
//    
//    // Find by created by
//    List<Escalation> findByCreatedBy(String createdBy);
    
    // Find escalations created between dates
    List<Escalation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by priority and status
    List<Escalation> findByPriorityAndStatus(String priority, String status);
    
    // Find by multiple statuses
    List<Escalation> findByStatusIn(List<String> statuses);
    
    // Find by priority ordered by created date
    List<Escalation> findByPriorityOrderByCreatedAtDesc(String priority);
    
    // Custom query to find escalations by email (any email field)
    @Query("SELECT e FROM Escalation e WHERE e.escalationEmailId = :email OR e.emailIdPrimaryResponsible = :email OR e.reportingLevelEmailId = :email")
    List<Escalation> findByAnyEmail(@Param("email") String email);
    
    // Custom query to find escalations with specific priority and created in last N days
    @Query("SELECT e FROM Escalation e WHERE e.priority = :priority AND e.createdAt >= :fromDate")
    List<Escalation> findByPriorityAndCreatedAfter(@Param("priority") String priority, @Param("fromDate") LocalDateTime fromDate);
    
    // Custom query to count escalations by status
    @Query("SELECT COUNT(e) FROM Escalation e WHERE e.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Custom query to find escalations that need attention (based on resolution period)
    @Query("SELECT e FROM Escalation e WHERE e.escalationResolutionPeriod = :period AND e.status != 'CLOSED'")
    List<Escalation> findPendingEscalationsByResolutionPeriod(@Param("period") String period);
    
    // Find escalations by description containing text (case insensitive)
    List<Escalation> findByDescriptionContainingIgnoreCase(String description);
    
    // Find escalations by action containing text (case insensitive)
    List<Escalation> findByActionContainingIgnoreCase(String action);
    
    // Find escalations by remarks containing text (case insensitive)
    List<Escalation> findByRemarksContainingIgnoreCase(String remarks);
    
    // Check if escalation exists by email
    boolean existsByEscalationEmailId(String escalationEmailId);
    
    // Find latest escalations (top N)
    List<Escalation> findTop10ByOrderByCreatedAtDesc();
    
    // Custom query for dashboard - count by priority
    @Query("SELECT e.priority, COUNT(e) FROM Escalation e GROUP BY e.priority")
    List<Object[]> countEscalationsByPriority();
    
    // Custom query for dashboard - count by status
    @Query("SELECT e.status, COUNT(e) FROM Escalation e GROUP BY e.status")
    List<Object[]> countEscalationsByStatus();
    
    @Query("SELECT e FROM Escalation e WHERE e.organization.id = :orgId AND e.id = :id AND e.deleted = false")
    Escalation findByOrgIdAndId(@Param("orgId")Long orgId, @Param("id")Long id);

    @Query("SELECT e FROM Escalation e WHERE e.organization.id = :id AND e.deleted = false")
	List<Escalation> getByOrgId(@Param("id")Long id);
}
