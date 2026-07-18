package ermorg.erm.dto.response;

import java.util.Date;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.WorkflowTriggerType;
import ermorg.erm.model.Approval;
import lombok.Data;

@Data
public class ApprovalResponse {
	private Long id;
	private Long approverId;
	private Long submitterId;
	private String pageLink;
	private ApprovalStatus status;
	private String comment;
	private Date notifiedAt;
	private Date createdAt;
	private String recordName;
	private String taggedMembers;
	private String recipientUserIds;
	private String sourceModule;
	private String sourceRecordId;
	private Long organizationId;
	private Long companyId;
	private Date assignedNotifiedAt;
	private Date decisionNotifiedAt;
	private Date dueAt;
	private Integer escalationLevel;
	private Date escalatedAt;
	private Date closedAt;
	private WorkflowTriggerType triggerType;
	private Long triggeredById;
	private Date triggeredAt;
	private WorkflowTriggerType escalationSource;

	public ApprovalResponse(Approval approval) {
		this.id = approval.getId();
		this.approverId = approval.getApprover() != null ? approval.getApprover().getId() : null;
		this.submitterId = approval.getSubmitter() != null ? approval.getSubmitter().getId() : null;
		this.pageLink = approval.getPageLink();
		this.status = approval.getStatus();
		this.comment = approval.getComment();
		this.notifiedAt = approval.getNotifiedAt();
		this.createdAt = approval.getCreatedAt();
		this.recordName = approval.getRecordName();
		this.taggedMembers = approval.getTaggedMembers();
		this.recipientUserIds = approval.getRecipientUserIds();
		this.sourceModule = approval.getSourceModule();
		this.sourceRecordId = approval.getSourceRecordId();
		this.organizationId = approval.getOrganization() != null ? approval.getOrganization().getId() : null;
		this.companyId = approval.getCompany() != null ? approval.getCompany().getId() : null;
		this.assignedNotifiedAt = approval.getAssignedNotifiedAt();
		this.decisionNotifiedAt = approval.getDecisionNotifiedAt();
		this.dueAt = approval.getDueAt();
		this.escalationLevel = approval.getEscalationLevel();
		this.escalatedAt = approval.getEscalatedAt();
		this.closedAt = approval.getClosedAt();
		this.triggerType = approval.getTriggerType();
		this.triggeredById = approval.getTriggeredBy() != null ? approval.getTriggeredBy().getId() : null;
		this.triggeredAt = approval.getTriggeredAt();
		this.escalationSource = approval.getEscalationSource();
	}
}
