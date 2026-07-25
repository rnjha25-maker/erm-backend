package ermorg.erm.serviceimpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.WorkflowTriggerType;
import ermorg.erm.model.Approval;
import ermorg.erm.repository.ApprovalRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApprovalEscalationScheduler {

    private final ApprovalRepository approvalRepository;
    private final NotificationService notificationService;

    @Value("${approval.escalation.enabled:true}")
    private boolean enabled;

    @Value("${approval.reminder.interval-ms:86400000}")
    private long reminderIntervalMs;

    @Value("${approval.escalation.interval-ms:86400000}")
    private long escalationIntervalMs;

    public ApprovalEscalationScheduler(ApprovalRepository approvalRepository, NotificationService notificationService) {
        this.approvalRepository = approvalRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${approval.escalation.fixed-delay:60000}")
    @Transactional
    public void processPendingEscalations() {
        if (!enabled) {
            log.info("Approval escalation scheduler is disabled.");
            return;
        }
        Date now = new Date();
        processReminders(now);
        processEscalations(now);
    }

    private void processReminders(Date now) {
        Date reminderBefore = new Date(now.getTime() - reminderIntervalMs);
        List<Approval> pending = approvalRepository.findPendingApprovalsDueForReminder(ApprovalStatus.PENDING,
                reminderBefore);
        for (Approval approval : pending) {
            if (approval.getTriggerType() == WorkflowTriggerType.MANUAL && approval.getTriggeredAt() == null) {
                continue;
            }
            notificationService.sendReminder(approval);
            approval.setReminderNotifiedAt(now);
            approvalRepository.save(approval);
            log.info("Sent automatic reminder for approval {}", approval.getId());
        }
    }

    private void processEscalations(Date now) {
        List<Approval> pending = approvalRepository.findAutomaticOverdueApprovals(ApprovalStatus.PENDING,
                WorkflowTriggerType.AUTOMATIC, now);
        for (Approval approval : pending) {
            if (approval.getDueAt() == null || approval.getDueAt().after(now)) {
                continue;
            }
            if (approval.getEscalatedAt() != null
                    && approval.getEscalatedAt().after(new Date(now.getTime() - escalationIntervalMs))) {
                continue;
            }
            approval.setEscalationLevel((approval.getEscalationLevel() == null ? 0 : approval.getEscalationLevel()) + 1);
            approval.setEscalatedAt(now);
            approval.setEscalationSource(WorkflowTriggerType.AUTOMATIC);
            approvalRepository.save(approval);
            notificationService.sendEscalation(approval);
            log.info("Escalated approval {} to level {}", approval.getId(), approval.getEscalationLevel());
        }
    }
}
