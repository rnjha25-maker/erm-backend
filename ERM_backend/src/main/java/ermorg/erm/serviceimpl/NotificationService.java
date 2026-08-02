package ermorg.erm.serviceimpl;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ermorg.erm.constant.NotificationChannel;
import ermorg.erm.constant.NotificationStatus;
import ermorg.erm.constant.WorkflowEventType;
import ermorg.erm.dto.response.NotificationResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Approval;
import ermorg.erm.model.Notification;
import ermorg.erm.model.Organization;
import ermorg.erm.model.User;
import ermorg.erm.model.UserDetail;
import ermorg.erm.repository.NotificationRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.UserContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\p{L}\\p{N} ._-]+)");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${approval.notifications.enabled:false}")
    private boolean notificationsEnabled;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public List<NotificationResponse> getMyNotifications() throws ResourceNotFoundException {
        User user = UserContext.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return notificationRepository.findByRecipientIdAndDeletedFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    public void sendApprovalSubmitted(Approval approval) {
        if (approval == null) {
            return;
        }
        notificationForRecipients(approval, WorkflowEventType.SUBMITTED,
                "Approval request submitted successfully.",
                buildSubmittedBody(approval), approval.getSubmitter());
    }

    public void sendApprovalAssigned(Approval approval) {
        if (approval == null) {
            return;
        }
        notificationForRecipients(approval, WorkflowEventType.ASSIGNED,
                "You have a new approval request.",
                buildAssignmentBody(approval), approval.getApprover());
        approval.setAssignedNotifiedAt(new Date());
    }

    public void sendApprovalDecision(Approval approval) {
        if (approval == null) {
            return;
        }
        notificationForRecipients(approval,
                approval.getStatus() == ermorg.erm.constant.ApprovalStatus.APPROVED
                        ? WorkflowEventType.APPROVED
                        : WorkflowEventType.REJECTED,
                "Approval " + approval.getStatus() + ": " + safeRecordName(approval),
                buildDecisionBody(approval), approval.getSubmitter());
        approval.setDecisionNotifiedAt(new Date());
    }

    public void sendEscalation(Approval approval) {
        if (approval == null) {
            return;
        }
        notificationForRecipients(approval, WorkflowEventType.ESCALATED,
                "Approval escalated: " + safeRecordName(approval),
                buildEscalationBody(approval), approval.getApprover());
    }

    public void sendReminder(Approval approval) {
        if (approval == null) {
            return;
        }
        notificationForRecipients(approval, WorkflowEventType.REMINDER_SENT,
                "Reminder for approval: " + safeRecordName(approval),
                buildReminderBody(approval), approval.getApprover());
    }

    private void notificationForRecipients(Approval approval, WorkflowEventType eventType, String subject, String body,
            User primaryRecipient) {
        Set<User> recipients = resolveRecipients(approval, eventType, primaryRecipient);
        for (User recipient : recipients) {
            Notification notification = new Notification();
            notification.setApproval(approval);
            notification.setRecipient(recipient);
            notification.setChannel(NotificationChannel.EMAIL);
            notification.setEventType(eventType);
            notification.setSourceModule(approval.getSourceModule());
            notification.setSourceRecordId(approval.getSourceRecordId());
            notification.setSubject(subject);
            notification.setBody(body);
            notification.setStatus(NotificationStatus.PENDING);
            try {
                if (notificationsEnabled) {
                    sendEmail(recipient, subject, body);
                    notification.setStatus(NotificationStatus.SENT);
                    notification.setSentAt(new Date());
                }
            } catch (Exception ex) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailureReason(ex.getMessage());
                log.warn("Approval notification failed for user {}: {}", recipient.getId(), ex.getMessage());
            }
            notificationRepository.save(notification);
        }
    }

    private Set<User> resolveRecipients(Approval approval, WorkflowEventType eventType, User primaryRecipient) {
        Set<User> recipients = new LinkedHashSet<>();
        if (primaryRecipient != null) {
            recipients.add(primaryRecipient);
        }
        if (eventType != WorkflowEventType.APPROVED && eventType != WorkflowEventType.REJECTED) {
            return recipients;
        }
        if (approval.getRecipientUserIds() != null && !approval.getRecipientUserIds().isBlank()) {
            for (String id : approval.getRecipientUserIds().split(",")) {
                try {
                    userRepository.findById(Long.parseLong(id.trim()))
                            .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                            .ifPresent(recipients::add);
                } catch (NumberFormatException ex) {
                    log.warn("Ignoring invalid approval recipient user id '{}'", id);
                }
            }
        }

        Organization organization = OrganizationContext.getOrganization();
        if (organization == null || approval.getTaggedMembers() == null || approval.getTaggedMembers().isBlank()) {
            return recipients;
        }

        List<User> users = userRepository.findActiveUsersByOrganizationId(organization.getId());
        List<String> mentions = extractMentions(approval.getTaggedMembers());
        for (String mention : mentions) {
            String normalizedMention = normalize(mention);
            users.stream()
                    .filter(user -> normalize(displayName(user)).equals(normalizedMention)
                            || normalize(user.getEmail()).equals(normalizedMention)
                            || normalize(phone(user)).equals(normalizedMention))
                    .findFirst()
                    .ifPresent(recipients::add);
        }
        return recipients;
    }

    private String buildSubmittedBody(Approval approval) {
        return "Approval request submitted successfully." + System.lineSeparator()
                + "Module: " + nullToBlank(approval.getSourceModule()) + System.lineSeparator()
                + "Record: " + safeRecordName(approval) + System.lineSeparator()
                + "Approver: " + displayName(approval.getApprover()) + System.lineSeparator()
                + "Date: " + new Date();
    }

    private List<String> extractMentions(String text) {
        List<String> mentions = new java.util.ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            mentions.add(matcher.group(1).trim());
        }
        return mentions;
    }

    private void sendEmail(User recipient, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildAssignmentBody(Approval approval) {
        return "You have a new approval request." + System.lineSeparator()
                + "Module: " + nullToBlank(approval.getSourceModule()) + System.lineSeparator()
                + "Record: " + safeRecordName(approval) + System.lineSeparator()
                + "Requester: " + displayName(approval.getSubmitter()) + System.lineSeparator()
                + "Link: " + approval.getPageLink();
    }

    private String buildDecisionBody(Approval approval) {
        boolean approved = approval.getStatus() == ermorg.erm.constant.ApprovalStatus.APPROVED;
        StringBuilder body = new StringBuilder();
        body.append(approved
                ? "Your approval request has been approved."
                : "Your approval request has been rejected.")
                .append(System.lineSeparator())
                .append("Module: ").append(nullToBlank(approval.getSourceModule())).append(System.lineSeparator())
                .append("Record: ").append(safeRecordName(approval)).append(System.lineSeparator())
                .append("Approver: ").append(displayName(approval.getApprover())).append(System.lineSeparator())
                .append("Date: ").append(approval.getClosedAt() == null ? new Date() : approval.getClosedAt())
                .append(System.lineSeparator())
                .append("Comment: ").append(nullToBlank(approval.getComment()));
        if (!approved) {
            body.append(System.lineSeparator())
                    .append("Reason: ").append(nullToBlank(approval.getComment())).append(System.lineSeparator())
                    .append("Root Cause: ").append(nullToBlank(approval.getRootCause())).append(System.lineSeparator())
                    .append("Action Taken: ").append(nullToBlank(approval.getActionTaken()));
        }
        body.append(System.lineSeparator()).append("Link: ").append(approval.getPageLink());
        return body.toString();
    }

    private String buildEscalationBody(Approval approval) {
        return "Approval escalated" + System.lineSeparator()
                + "Record: " + safeRecordName(approval) + System.lineSeparator()
                + "Current level: " + approval.getEscalationLevel();
    }

    private String buildReminderBody(Approval approval) {
        return "Reminder for pending approval" + System.lineSeparator()
                + "Record: " + safeRecordName(approval) + System.lineSeparator()
                + "Link: " + approval.getPageLink();
    }

    private String safeRecordName(Approval approval) {
        return approval.getRecordName() == null || approval.getRecordName().isBlank()
                ? "ERM record"
                : approval.getRecordName();
    }

    private String displayName(User user) {
        if (user == null) {
            return "";
        }
        UserDetail detail = user.getUserDetail();
        if (detail == null) {
            return user.getEmail();
        }
        String name = String.join(" ",
                nullToBlank(detail.getFirstName()),
                nullToBlank(detail.getMiddleName()),
                nullToBlank(detail.getLastName())).trim().replaceAll("\\s+", " ");
        return name.isBlank() ? user.getEmail() : name;
    }

    private String phone(User user) {
        UserDetail detail = user != null ? user.getUserDetail() : null;
        return detail != null ? detail.getPhone() : "";
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+]", "");
    }
}
