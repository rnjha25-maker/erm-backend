package ermorg.erm.model;

import java.util.Date;

import ermorg.erm.constant.NotificationChannel;
import ermorg.erm.constant.NotificationStatus;
import ermorg.erm.constant.WorkflowEventType;
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
@Table(name = "notification")
public class Notification extends BaseModel {

	@ManyToOne
	@JoinColumn(name = "recipient_user_id")
	private User recipient;

	@ManyToOne
	@JoinColumn(name = "approval_id")
	private Approval approval;

	@Enumerated(EnumType.STRING)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	private WorkflowEventType eventType;

	@Column(name = "source_module")
	private String sourceModule;

	@Column(name = "source_record_id")
	private String sourceRecordId;

	@Column(length = 1000)
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String body;

	private String failureReason;
	private Integer retryCount = 0;
	private Date nextRetryAt;
	private Date sentAt;

	@Enumerated(EnumType.STRING)
	private NotificationStatus status = NotificationStatus.PENDING;
}
