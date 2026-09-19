package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.*;
import ermorg.erm.repository.*;
import ermorg.erm.util.OrganizationContext;
import static org.mockito.Mockito.mock;

class EvidenceAssociationTest {
    @Test
    void editingTreatmentWithoutDocumentFieldKeepsUploadedEvidence() throws Exception {
        var service = new RiskTreatmentService();
        var treatments = mock(RiskResponseTreatmentRepository.class);
        var risks = mock(RiskRepository.class);
        var users = mock(UserRepository.class);
        ReflectionTestUtils.setField(service, "riskResponseTreatmentRepository", treatments);
        ReflectionTestUtils.setField(service, "riskRepository", risks);
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "fieldMapperUtils", mock(FieldMapperUtils.class));
        var org = new Organization(); org.setId(1L);
        var risk = new Risk(); risk.setId(2L);
        var user = new User(); user.setId(3L); user.setDeleted(false);
        var treatment = new RiskResponseTreatment(); treatment.setId(4L); treatment.setDeleted(false);
        UUID documentId = UUID.randomUUID(); treatment.setSupportingEvidenceDocument(documentId);
        when(risks.getRisksByOrgIdAndRiskId(1L, 2L)).thenReturn(risk);
        when(users.findById(3L)).thenReturn(Optional.of(user));
        when(treatments.findById(4L)).thenReturn(Optional.of(treatment));
        when(treatments.save(any())).thenAnswer(i -> i.getArgument(0));
        var request = new RiskResponseTreatmentDto();
        request.setRiskId(2L); request.setRiskReporting(3L); request.setRiskResponseTreatmentId(4L);
        OrganizationContext.setOrganization(org);
        try {
            assertThat(service.save(request).getSupportingEvidenceDocument()).isEqualTo(documentId);
            assertThat(treatment.getSupportingEvidenceDocument()).isEqualTo(documentId);
        } finally { OrganizationContext.clear(); }
    }
}
