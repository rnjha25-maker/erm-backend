package ermorg.erm.serviceimpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.model.Approval;
import ermorg.erm.repository.ApprovalRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApprovalEscalationScheduler {

    private final ApprovalRepository approvalRepository;
    private final NotificationService notificationService;

    @Value("${approval.escalation.enabled:false}")
    private boolean enabled;

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
        List<Approval> pending = approvalRepository.findByStatusAndDueAtBeforeAndDeletedFalse(ApprovalStatus.PENDING, new Date());
        for (Approval approval : pending) {
            if (approval.getDueAt() == null || approval.getDueAt().after(new Date())) {
                continue;
            }
            approval.setEscalationLevel((approval.getEscalationLevel() == null ? 0 : approval.getEscalationLevel()) + 1);
            approval.setEscalatedAt(new Date());
            approvalRepository.save(approval);
            notificationService.sendEscalation(approval);
            log.info("Escalated approval {} to level {}", approval.getId(), approval.getEscalationLevel());
        }
    }
}
