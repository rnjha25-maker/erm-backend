package ermorg.erm.model;

import java.util.Date;

import ermorg.erm.constant.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "approval")
public class Approval extends BaseModel {

	@ManyToOne
	@JoinColumn(name = "approver_id")
	private User approver;

	@ManyToOne
	@JoinColumn(name = "submitter_id")
	private User submitter;

	@Column(name = "page_link", length = 1000)
	private String pageLink;

	@Enumerated(EnumType.STRING)
	private ApprovalStatus status = ApprovalStatus.PENDING;

	@Column(length = 2000)
	private String comment;

	private Date notifiedAt;

	private String recordName;

	@Column(length = 1000)
	private String taggedMembers;

	@Column(length = 1000)
	private String recipientUserIds;
}
