package ermorg.erm.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import ermorg.erm.constant.ApprovalStatus;
import ermorg.erm.constant.WorkflowTriggerType;
import ermorg.erm.dto.response.ApprovalResponse;
import ermorg.erm.dto.riskDTO.ApprovalDecisionRequest;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.model.Approval;
import ermorg.erm.model.User;
import ermorg.erm.repository.ApprovalRepository;
import ermorg.erm.repository.NotificationRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.UserContext;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApprovalService approvalService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createApprovalShouldSendAssignmentNotification() throws Exception {
        User approver = new User();
        approver.setId(2L);
        approver.setEmail("approver@example.com");

        User submitter = new User();
        submitter.setId(1L);
        submitter.setEmail("submitter@example.com");

        Approval approval = new Approval();
        approval.setId(10L);
        approval.setApprover(approver);
        approval.setSubmitter(submitter);
        approval.setStatus(ApprovalStatus.PENDING);

        ApprovalRequest request = new ApprovalRequest();
        request.setApproverId(2L);
        request.setSubmitterId(1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(userRepository.findById(1L)).thenReturn(Optional.of(submitter));
        when(approvalRepository.save(any(Approval.class))).thenReturn(approval);

        ApprovalResponse response = approvalService.createApproval(request);

        assertNotNull(response);
        verify(notificationService).sendApprovalAssigned(any(Approval.class));
    }

    @Test
    void decideShouldSendDecisionNotification() throws Exception {
        User approver = new User();
        approver.setId(3L);
        approver.setEmail("approver@example.com");

        User submitter = new User();
        submitter.setId(4L);
        submitter.setEmail("submitter@example.com");

        Approval approval = new Approval();
        approval.setId(11L);
        approval.setApprover(approver);
        approval.setSubmitter(submitter);
        approval.setStatus(ApprovalStatus.PENDING);

        UserContext.seetUser(approver);

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setStatus(ApprovalStatus.APPROVED);
        request.setComment("Looks good");

        when(approvalRepository.findById(11L)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any(Approval.class))).thenReturn(approval);

        ApprovalResponse response = approvalService.decide(11L, request);

        assertNotNull(response);
        verify(notificationService).sendApprovalDecision(any(Approval.class));
    }

    @Test
    void createManualApprovalShouldNotSendAssignmentNotification() throws Exception {
        User approver = new User();
        approver.setId(2L);

        User submitter = new User();
        submitter.setId(1L);

        Approval approval = new Approval();
        approval.setId(12L);
        approval.setApprover(approver);
        approval.setSubmitter(submitter);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setTriggerType(WorkflowTriggerType.MANUAL);

        ApprovalRequest request = new ApprovalRequest();
        request.setApproverId(2L);
        request.setSubmitterId(1L);
        request.setTriggerType(WorkflowTriggerType.MANUAL);

        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(userRepository.findById(1L)).thenReturn(Optional.of(submitter));
        when(approvalRepository.save(any(Approval.class))).thenReturn(approval);

        ApprovalResponse response = approvalService.createApproval(request);

        assertEquals(WorkflowTriggerType.MANUAL, response.getTriggerType());
        verify(notificationService, org.mockito.Mockito.never()).sendApprovalAssigned(any(Approval.class));
    }

    @Test
    void triggerManualApprovalShouldSetAuditAndNotify() throws Exception {
        User approver = new User();
        approver.setId(2L);

        User submitter = new User();
        submitter.setId(1L);

        Approval approval = new Approval();
        approval.setId(13L);
        approval.setApprover(approver);
        approval.setSubmitter(submitter);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setTriggerType(WorkflowTriggerType.MANUAL);

        UserContext.seetUser(submitter);

        when(approvalRepository.findById(13L)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any(Approval.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalResponse response = approvalService.trigger(13L);

        assertEquals(1L, response.getTriggeredById());
        assertNotNull(response.getTriggeredAt());
        verify(notificationService).sendApprovalAssigned(any(Approval.class));
    }
}
