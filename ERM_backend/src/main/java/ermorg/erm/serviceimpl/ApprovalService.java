package ermorg.erm.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.RoleTypeCode;
import ermorg.erm.constant.WorkflowTriggerType;
import ermorg.erm.dto.response.ApprovalLoginTargetResponse;
import ermorg.erm.dto.response.ApprovalResponse;
import ermorg.erm.dto.riskDTO.ApprovalDecisionRequest;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Approval;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Role;
import ermorg.erm.model.User;
import ermorg.erm.repository.ApprovalRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.IApprovalService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.UserContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApprovalService implements IApprovalService {

	private final ApprovalRepository approvalRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	public ApprovalService(ApprovalRepository approvalRepository, UserRepository userRepository,
			NotificationService notificationService) {
		this.approvalRepository = approvalRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	@Override
	@Transactional
	public ApprovalResponse createApproval(ApprovalRequest request) throws ResourceNotFoundException {
		User approver = userRepository.findById(request.getApproverId())
				.filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Approver not found."));
		User submitter = userRepository.findById(request.getSubmitterId())
				.filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Submitter not found."));

		Approval approval = new Approval();
		approval.setApprover(approver);
		approval.setSubmitter(submitter);
		approval.setOrganization(OrganizationContext.getOrganization());
		approval.setCompany(CompanyContext.getCompany());
		approval.setPageLink(request.getPageLink());
		approval.setRecordName(request.getRecordName());
		approval.setTaggedMembers(request.getTaggedMembers());
		approval.setRecipientUserIds(request.getRecipientUserIds() == null ? null
				: request.getRecipientUserIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
		approval.setSourceModule(request.getSourceModule());
		approval.setSourceRecordId(request.getSourceRecordId());
		approval.setDueAt(request.getDueAt());
		approval.setTriggerType(request.getTriggerType() == null ? WorkflowTriggerType.AUTOMATIC : request.getTriggerType());
		approval.setStatus(ApprovalStatus.PENDING);
		Approval saved = approvalRepository.save(approval);
		if (saved.getTriggerType() == WorkflowTriggerType.AUTOMATIC) {
			notificationService.sendApprovalAssigned(saved);
		}
		return new ApprovalResponse(saved);
	}

	@Override
	@Transactional
	public ApprovalResponse decide(Long approvalId, ApprovalDecisionRequest request) throws ResourceNotFoundException {
		Approval approval = approvalRepository.findById(approvalId)
				.filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Approval not found."));

		User currentUser = UserContext.getUser();
		if (currentUser != null && approval.getApprover() != null
				&& !Objects.equals(currentUser.getId(), approval.getApprover().getId())) {
			throw new ResourceNotFoundException("Approval is not assigned to the logged-in user.");
		}
		if (request.getStatus() != ApprovalStatus.APPROVED && request.getStatus() != ApprovalStatus.REJECTED) {
			throw new ResourceNotFoundException("Decision must be APPROVED or REJECTED.");
		}

		if (approval.getStatus() != ApprovalStatus.PENDING) {
			throw new ResourceNotFoundException("Approval already has a final decision.");
		}

		approval.setStatus(request.getStatus());
		approval.setComment(request.getComment());
		approval.setNotifiedAt(new Date());
		approval.setClosedAt(new Date());
		Approval saved = approvalRepository.save(approval);
		notificationService.sendApprovalDecision(saved);
		return new ApprovalResponse(saved);
	}

	@Override
	@Transactional
	public ApprovalResponse trigger(Long approvalId) throws ResourceNotFoundException {
		Approval approval = getActiveApproval(approvalId);
		assertPending(approval);
		assertAuthorizedForWorkflowAction(approval);
		if (approval.getTriggerType() != WorkflowTriggerType.MANUAL) {
			throw new ResourceNotFoundException("Only MANUAL approvals can be manually triggered.");
		}
		if (approval.getTriggeredAt() != null) {
			throw new ResourceNotFoundException("Manual workflow has already been triggered.");
		}

		approval.setTriggeredBy(UserContext.getUser());
		approval.setTriggeredAt(new Date());
		Approval saved = approvalRepository.save(approval);
		notificationService.sendApprovalAssigned(saved);
		return new ApprovalResponse(saved);
	}

	@Override
	@Transactional
	public ApprovalResponse escalate(Long approvalId) throws ResourceNotFoundException {
		Approval approval = getActiveApproval(approvalId);
		assertPending(approval);
		assertAuthorizedForWorkflowAction(approval);

		approval.setEscalationLevel((approval.getEscalationLevel() == null ? 0 : approval.getEscalationLevel()) + 1);
		approval.setEscalatedAt(new Date());
		approval.setEscalationSource(WorkflowTriggerType.MANUAL);
		Approval saved = approvalRepository.save(approval);
		notificationService.sendEscalation(saved);
		log.info("Manually escalated approval {} to level {}", saved.getId(), saved.getEscalationLevel());
		return new ApprovalResponse(saved);
	}

	@Override
	@Transactional
	public ApprovalResponse sendReminder(Long approvalId) throws ResourceNotFoundException {
		Approval approval = getActiveApproval(approvalId);
		assertPending(approval);
		assertAuthorizedForWorkflowAction(approval);

		notificationService.sendReminder(approval);
		return new ApprovalResponse(approval);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ApprovalResponse> getMyPendingApprovals() throws ResourceNotFoundException {
		User user = UserContext.getUser();
		if (user == null) {
			throw new ResourceNotFoundException("User not found.");
		}
		return approvalRepository
				.findByApproverIdAndStatusAndDeletedFalseOrderByCreatedAtAsc(user.getId(), ApprovalStatus.PENDING)
				.stream()
				.map(ApprovalResponse::new)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public ApprovalLoginTargetResponse getLoginTarget() throws ResourceNotFoundException {
		List<ApprovalResponse> pending = getMyPendingApprovals();
		String redirectUrl = null;
		if (pending.size() == 1) {
			redirectUrl = pending.get(0).getPageLink();
		} else if (pending.size() > 1) {
			redirectUrl = "/approvals/pending";
		}
		return new ApprovalLoginTargetResponse(redirectUrl, pending);
	}

	private Company resolveCompany() {
		Company company = CompanyContext.getCompany();
		return company != null ? company : null;
	}

	private Organization resolveOrganization() {
		Organization organization = OrganizationContext.getOrganization();
		return organization != null ? organization : null;
	}

	private Approval getActiveApproval(Long approvalId) throws ResourceNotFoundException {
		return approvalRepository.findById(approvalId)
				.filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Approval not found."));
	}

	private void assertPending(Approval approval) throws ResourceNotFoundException {
		if (approval.getStatus() != ApprovalStatus.PENDING || approval.getClosedAt() != null) {
			throw new ResourceNotFoundException("Closed approvals cannot be modified.");
		}
	}

	private void assertAuthorizedForWorkflowAction(Approval approval) throws ResourceNotFoundException {
		User currentUser = UserContext.getUser();
		if (currentUser == null) {
			throw new ResourceNotFoundException("User not found.");
		}
		if (isSameUser(currentUser, approval.getSubmitter()) || isSameUser(currentUser, approval.getApprover())
				|| hasAdminRole(currentUser)) {
			return;
		}
		throw new ResourceNotFoundException("User is not authorized for this approval workflow action.");
	}

	private boolean isSameUser(User left, User right) {
		return left != null && right != null && Objects.equals(left.getId(), right.getId());
	}

	private boolean hasAdminRole(User user) {
		if (user.getRoles() == null) {
			return false;
		}
		return user.getRoles().stream()
				.map(Role::getRoleType)
				.filter(Objects::nonNull)
				.map(roleType -> roleType.getCode())
				.filter(Objects::nonNull)
				.anyMatch(code -> RoleTypeCode.SUPER_ADMIN.getCode().equalsIgnoreCase(code)
						|| RoleTypeCode.ORG_ADMIN.getCode().equalsIgnoreCase(code)
						|| RoleTypeCode.COMPANY_ADMIN.getCode().equalsIgnoreCase(code));
	}
}
