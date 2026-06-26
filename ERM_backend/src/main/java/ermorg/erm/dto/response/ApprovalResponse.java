package ermorg.erm.dto.response;

import java.util.Date;

import ermorg.erm.constant.ApprovalStatus;
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
	}
}
