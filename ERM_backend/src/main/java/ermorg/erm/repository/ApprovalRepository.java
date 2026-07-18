package ermorg.erm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.WorkflowTriggerType;
import ermorg.erm.model.Approval;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
	List<Approval> findByApproverIdAndStatusAndDeletedFalseOrderByCreatedAtAsc(Long approverId, ApprovalStatus status);

	List<Approval> findByStatusAndDeletedFalse(ApprovalStatus status);

	List<Approval> findByStatusAndDueAtBeforeAndDeletedFalse(ApprovalStatus status, Date dueAt);

	@Query("""
			select approval
			from Approval approval
			where approval.status = :status
			  and (approval.triggerType = :triggerType or approval.triggerType is null)
			  and approval.dueAt < :dueAt
			  and approval.deleted = false
			""")
	List<Approval> findAutomaticOverdueApprovals(ApprovalStatus status, WorkflowTriggerType triggerType, Date dueAt);
}
