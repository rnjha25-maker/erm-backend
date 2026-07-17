package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.ApprovalLoginTargetResponse;
import ermorg.erm.dto.response.ApprovalResponse;
import ermorg.erm.dto.riskDTO.ApprovalDecisionRequest;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IApprovalService {
	ApprovalResponse createApproval(ApprovalRequest request) throws ResourceNotFoundException;

	ApprovalResponse decide(Long approvalId, ApprovalDecisionRequest request) throws ResourceNotFoundException;

	ApprovalResponse trigger(Long approvalId) throws ResourceNotFoundException;

	ApprovalResponse escalate(Long approvalId) throws ResourceNotFoundException;

	ApprovalResponse sendReminder(Long approvalId) throws ResourceNotFoundException;

	List<ApprovalResponse> getMyPendingApprovals() throws ResourceNotFoundException;

	ApprovalLoginTargetResponse getLoginTarget() throws ResourceNotFoundException;
}
