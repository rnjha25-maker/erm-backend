package ermorg.erm.evaluation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ermorg.erm.model.*;
import ermorg.erm.util.*;
import ermorg.erm.serviceimpl.RiskTreatmentService;

class PeriodEvidenceTest {
    @Test void legacyDeleteCannotRemoveDocumentRetainedByHistory() {
        var service=new RiskTreatmentService();var history=mock(PeriodEvaluationService.class);
        var storage=mock(RestTemplate.class);
        ReflectionTestUtils.setField(service,"periodEvaluations",history);
        ReflectionTestUtils.setField(service,"restTemplate",storage);
        doThrow(new IllegalArgumentException("Evidence is retained in evaluation history and cannot be deleted."))
            .when(history).guardTreatmentEvidenceDeletion(10L,"document");
        assertThatThrownBy(()->service.deleteEvidence(10L,"document")).hasMessageContaining("retained in evaluation history");
        verifyNoInteractions(storage);
    }
    @Test void uploadUsesStorageContractAndRejectsEmptyFile() {
        var storage=mock(RestTemplate.class);var client=new PeriodEvidenceClient(storage,"http://storage:8085/");
        String id="dc615167-19a8-460f-aea0-2e3bf21b1e34";
        when(storage.postForObject(eq("http://storage:8085/upload"),any(),eq(Map.class))).thenReturn(Map.of("data",Map.of("documentId",id)));
        var org=new Organization();org.setId(1L);var company=new Company();company.setId(2L);
        OrganizationContext.setOrganization(org);CompanyContext.setCompany(company);
        try {
            assertThat(client.upload(new MockMultipartFile("file","proof.pdf","application/pdf",new byte[]{1,2}))).isEqualTo(id);
            assertThatIllegalArgumentException().isThrownBy(()->client.upload(new MockMultipartFile("file",new byte[0])));
        }finally{OrganizationContext.clear();CompanyContext.clear();}
    }
}
