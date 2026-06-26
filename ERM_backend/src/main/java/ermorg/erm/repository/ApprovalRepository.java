package ermorg.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.model.Approval;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
	List<Approval> findByApproverIdAndStatusAndDeletedFalseOrderByCreatedAtAsc(Long approverId, ApprovalStatus status);
}
