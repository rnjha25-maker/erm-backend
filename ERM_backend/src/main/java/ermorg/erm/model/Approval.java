package ermorg.erm.model;

import java.util.Date;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.WorkflowTriggerType;
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

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(name = "page_link", length = 1000)
	private String pageLink;

	@Enumerated(EnumType.STRING)
	private ApprovalStatus status = ApprovalStatus.PENDING;

	@Column(length = 2000)
	private String comment;

	@Column(name = "root_cause", columnDefinition = "TEXT")
	private String rootCause;

	@Column(name = "action_taken", columnDefinition = "TEXT")
	private String actionTaken;

	private Date notifiedAt;
	@Column(name = "assigned_notified_at")
	private Date assignedNotifiedAt;
	@Column(name = "decision_notified_at")
	private Date decisionNotifiedAt;
	@Column(name = "reminder_notified_at")
	private Date reminderNotifiedAt;
	@Column(name = "due_at")
	private Date dueAt;
	@Column(name = "escalation_level")
	private Integer escalationLevel = 0;
	@Column(name = "escalated_at")
	private Date escalatedAt;
	@Column(name = "closed_at")
	private Date closedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "trigger_type")
	private WorkflowTriggerType triggerType = WorkflowTriggerType.AUTOMATIC;

	@ManyToOne
	@JoinColumn(name = "triggered_by_id")
	private User triggeredBy;

	@Column(name = "triggered_at")
	private Date triggeredAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "escalation_source")
	private WorkflowTriggerType escalationSource;

	@Column(name = "source_module")
	private String sourceModule;

	@Column(name = "source_record_id")
	private String sourceRecordId;

	private String recordName;

	@Column(length = 1000)
	private String taggedMembers;

	@Column(length = 1000)
	private String recipientUserIds;
}
