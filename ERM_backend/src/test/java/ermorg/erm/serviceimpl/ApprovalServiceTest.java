package ermorg.erm.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        verify(notificationService).sendApprovalSubmitted(any(Approval.class));
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
        request.setRootCause("Optional root cause");
        request.setActionTaken("Optional action");

        when(approvalRepository.findById(11L)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any(Approval.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalResponse response = approvalService.decide(11L, request);

        assertNotNull(response);
        assertEquals("Optional root cause", response.getRootCause());
        assertEquals("Optional action", response.getActionTaken());
        verify(notificationService).sendApprovalDecision(any(Approval.class));
    }

    @Test
    void rejectShouldRequireCommentRootCauseAndActionTaken() {
        User approver = new User();
        approver.setId(3L);

        Approval approval = new Approval();
        approval.setId(99L);
        approval.setApprover(approver);
        approval.setStatus(ApprovalStatus.PENDING);

        UserContext.seetUser(approver);

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setStatus(ApprovalStatus.REJECTED);
        request.setComment("Insufficient evidence");

        when(approvalRepository.findById(99L)).thenReturn(Optional.of(approval));

        Exception exception = assertThrows(Exception.class, () -> approvalService.decide(99L, request));

        assertEquals("Root cause is required when rejecting an approval.", exception.getMessage());
    }

    @Test
    void rejectShouldPersistDecisionDetailsAndNotify() throws Exception {
        User approver = new User();
        approver.setId(3L);

        User submitter = new User();
        submitter.setId(4L);

        Approval approval = new Approval();
        approval.setId(15L);
        approval.setApprover(approver);
        approval.setSubmitter(submitter);
        approval.setStatus(ApprovalStatus.PENDING);

        UserContext.seetUser(approver);

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setStatus(ApprovalStatus.REJECTED);
        request.setComment("Evidence is missing");
        request.setRootCause("Control owner did not upload evidence");
        request.setActionTaken("Returned for evidence upload");

        when(approvalRepository.findById(15L)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any(Approval.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalResponse response = approvalService.decide(15L, request);

        assertEquals(ApprovalStatus.REJECTED, response.getStatus());
        assertEquals("Evidence is missing", response.getComment());
        assertEquals("Control owner did not upload evidence", response.getRootCause());
        assertEquals("Returned for evidence upload", response.getActionTaken());
        assertNotNull(response.getClosedAt());
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

    @Test
    void createApprovalShouldPersistRequesterAndApproverContext() throws Exception {
        User approver = new User();
        approver.setId(2L);

        User submitter = new User();
        submitter.setId(1L);

        ApprovalRequest request = new ApprovalRequest();
        request.setApproverId(2L);
        request.setSubmitterId(1L);
        request.setSourceModule("Risk");
        request.setSourceRecordId("RISK-1");

        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(userRepository.findById(1L)).thenReturn(Optional.of(submitter));
        when(approvalRepository.save(any(Approval.class))).thenAnswer(invocation -> {
            Approval saved = invocation.getArgument(0);
            saved.setId(16L);
            return saved;
        });

        ApprovalResponse response = approvalService.createApproval(request);

        assertEquals(1L, response.getRequestedBy());
        assertEquals(2L, response.getApprovedBy());
        assertEquals("Risk", response.getSourceModule());
        ArgumentCaptor<Approval> approvalCaptor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalRepository).save(approvalCaptor.capture());
        assertEquals("RISK-1", approvalCaptor.getValue().getSourceRecordId());
    }
}
