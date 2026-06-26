package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.NotificationChannel;
import ermorg.erm.constant.NotificationStatus;
import ermorg.erm.dto.response.ApprovalLoginTargetResponse;
import ermorg.erm.dto.response.ApprovalResponse;
import ermorg.erm.dto.riskDTO.ApprovalDecisionRequest;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Approval;
import ermorg.erm.model.Notification;
import ermorg.erm.model.Organization;
import ermorg.erm.model.User;
import ermorg.erm.model.UserDetail;
import ermorg.erm.repository.ApprovalRepository;
import ermorg.erm.repository.NotificationRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.IApprovalService;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.UserContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApprovalService implements IApprovalService {

	private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\p{L}\\p{N} ._-]+)");

	private final ApprovalRepository approvalRepository;
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	public ApprovalService(ApprovalRepository approvalRepository, NotificationRepository notificationRepository,
			UserRepository userRepository, ObjectProvider<JavaMailSender> mailSenderProvider) {
		this.approvalRepository = approvalRepository;
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.mailSenderProvider = mailSenderProvider;
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
		approval.setPageLink(request.getPageLink());
		approval.setRecordName(request.getRecordName());
		approval.setTaggedMembers(request.getTaggedMembers());
		approval.setRecipientUserIds(request.getRecipientUserIds() == null ? null
				: request.getRecipientUserIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
		approval.setStatus(ApprovalStatus.PENDING);
		return new ApprovalResponse(approvalRepository.save(approval));
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

		approval.setStatus(request.getStatus());
		approval.setComment(request.getComment());
		approval.setNotifiedAt(new Date());
		Approval saved = approvalRepository.save(approval);
		notifyMembers(saved);
		return new ApprovalResponse(saved);
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

	private void notifyMembers(Approval approval) {
		Set<User> recipients = resolveRecipients(approval);
		for (User recipient : recipients) {
			Notification notification = new Notification();
			notification.setApproval(approval);
			notification.setRecipient(recipient);
			notification.setChannel(NotificationChannel.EMAIL);
			try {
				sendEmail(approval, recipient);
				notification.setStatus(NotificationStatus.SENT);
				notification.setSentAt(new Date());
			} catch (Exception ex) {
				notification.setStatus(NotificationStatus.FAILED);
				log.warn("Approval notification failed for user {}: {}", recipient.getId(), ex.getMessage());
			}
			notificationRepository.save(notification);
		}
	}

	private Set<User> resolveRecipients(Approval approval) {
		Set<User> recipients = new LinkedHashSet<>();
		if (approval.getSubmitter() != null) {
			recipients.add(approval.getSubmitter());
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

	private List<String> extractMentions(String text) {
		List<String> mentions = new ArrayList<>();
		Matcher matcher = MENTION_PATTERN.matcher(text);
		while (matcher.find()) {
			mentions.add(matcher.group(1).trim());
		}
		return mentions;
	}

	private void sendEmail(Approval approval, User recipient) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
			return;
		}
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(recipient.getEmail());
		message.setSubject("Approval " + approval.getStatus() + ": " + safeRecordName(approval));
		message.setText(buildEmailBody(approval));
		mailSender.send(message);
	}

	private String buildEmailBody(Approval approval) {
		return "Approver: " + displayName(approval.getApprover()) + System.lineSeparator()
				+ "Decision: " + approval.getStatus() + System.lineSeparator()
				+ "Record: " + safeRecordName(approval) + System.lineSeparator()
				+ "Comment: " + (approval.getComment() == null ? "" : approval.getComment()) + System.lineSeparator()
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
