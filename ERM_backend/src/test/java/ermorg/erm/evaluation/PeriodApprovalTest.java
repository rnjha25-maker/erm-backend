package ermorg.erm.evaluation;
import ermorg.erm.serviceimpl.PeriodEvaluationService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.serviceimpl.*;
import ermorg.erm.repository.*;
import ermorg.erm.model.*;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.util.*;

class PeriodApprovalTest {
    @Test void newNamespaceValidatesCompletedPeriodAndDoesNotQueryLegacyRepositories() throws Exception {
        var periods=mock(PeriodEvaluationService.class);
        var validator=new ApprovalSourceRecordValidator(null,null,null,null,null,null,null,null,null,null,null);
        ReflectionTestUtils.setField(validator,"periodEvaluations",periods);
        validator.validate("periodEvaluation","42");
        verify(periods).assertApprovalSource(42L);
        doThrow(new IllegalArgumentException("Complete the evaluation first.")).when(periods).assertApprovalSource(43L);
        assertThatThrownBy(()->validator.validate("periodEvaluation","43")).hasMessageContaining("Complete");
    }
    @Test void crossCompanyPeriodApprovalDoesNotSaveOrNotify() {
        var approvals=mock(ApprovalRepository.class);var users=mock(UserRepository.class);
        var notifications=mock(NotificationService.class);var validator=mock(ApprovalSourceRecordValidator.class);
        var service=new ApprovalService(approvals,users,notifications,validator);
        var org=new Organization();org.setId(10L);var company=new Company();company.setId(20L);
        var other=new Company();other.setId(21L);
        var submitter=new User();submitter.setId(30L);submitter.setOrganization(org);submitter.setCompany(company);
        var approver=new User();approver.setId(31L);approver.setOrganization(org);approver.setCompany(other);
        when(users.findById(30L)).thenReturn(Optional.of(submitter));when(users.findById(31L)).thenReturn(Optional.of(approver));
        var request=new ApprovalRequest();request.setApproverId(31L);request.setSubmitterId(30L);
        request.setSourceModule("periodEvaluation");request.setSourceRecordId("42");request.setPageLink("/period-evaluations/42");request.setRecordName("Review Q2");
        OrganizationContext.setOrganization(org);CompanyContext.setCompany(company);UserContext.seetUser(submitter);
        try {
            assertThatThrownBy(()->service.createApproval(request)).hasMessageContaining("selected company");
            verifyNoInteractions(approvals,notifications,validator);
        }finally{OrganizationContext.clear();CompanyContext.clear();UserContext.clear();}
    }
}
