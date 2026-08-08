package ermorg.erm.dto.response;

import java.util.Date;

import ermorg.erm.constant.NotificationChannel;
import ermorg.erm.constant.NotificationStatus;
import ermorg.erm.constant.WorkflowEventType;
import ermorg.erm.model.Notification;
import lombok.Data;

@Data
public class NotificationResponse {
	private Long id;
	private Long recipientUserId;
	private Long approvalId;
	private NotificationChannel channel;
	private WorkflowEventType eventType;
	private String sourceModule;
	private String sourceRecordId;
	private String subject;
	private String body;
	private NotificationStatus status;
	private String failureReason;
	private Integer retryCount;
	private Date nextRetryAt;
	private Date sentAt;
	private Date createdAt;

	public NotificationResponse(Notification notification) {
		this.id = notification.getId();
		this.recipientUserId = notification.getRecipient() != null ? notification.getRecipient().getId() : null;
		this.approvalId = notification.getApproval() != null ? notification.getApproval().getId() : null;
		this.channel = notification.getChannel();
		this.eventType = notification.getEventType();
		this.sourceModule = notification.getSourceModule();
		this.sourceRecordId = notification.getSourceRecordId();
		this.subject = notification.getSubject();
		this.body = notification.getBody();
		this.status = notification.getStatus();
		this.failureReason = notification.getFailureReason();
		this.retryCount = notification.getRetryCount();
		this.nextRetryAt = notification.getNextRetryAt();
		this.sentAt = notification.getSentAt();
		this.createdAt = notification.getCreatedAt();
	}
}
