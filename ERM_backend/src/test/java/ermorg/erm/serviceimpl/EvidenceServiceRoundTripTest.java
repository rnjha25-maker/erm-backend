package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.*;
import ermorg.erm.repository.RiskResponseTreatmentRepository;
import ermorg.erm.util.OrganizationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

class EvidenceServiceRoundTripTest {
    @Test
    void forwardsActualBytesAndLinksReturnedDocumentThenLoadsAndDownloadsIt() throws Exception {
        var service = new RiskTreatmentService();
        var repository = mock(RiskResponseTreatmentRepository.class);
        var fields = mock(FieldMapperUtils.class);
        var rest = new RestTemplate();
        var storage = MockRestServiceServer.bindTo(rest).build();
        ReflectionTestUtils.setField(service, "riskResponseTreatmentRepository", repository);
        ReflectionTestUtils.setField(service, "fieldMapperUtils", fields);
        ReflectionTestUtils.setField(service, "restTemplate", rest);
        ReflectionTestUtils.setField(service, "storageServiceUrl", "http://test-storage");
        var org = new Organization(); org.setId(1L);
        var risk = new Risk(); risk.setId(2L);
        var user = new User(); user.setId(3L);
        var treatment = new RiskResponseTreatment(); treatment.setId(4L);
        treatment.setRisk(risk); treatment.setRiskReporting(user);
        when(repository.findById(4L)).thenReturn(Optional.of(treatment));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        UUID documentId = UUID.randomUUID();
        byte[] bytes = "%PDF-1.4\n% Evidence bytes including binary: \u0000\u00ff\n%%EOF".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String document = new ObjectMapper().writeValueAsString(Map.of("status", "SUCCESS", "data", Map.of(
            "documentId", documentId.toString(), "fileName", "evidence", "fileExtension", ".pdf",
            "contentType", "application/pdf", "purpose", "risk-response-treatment", "fileContent", base64)));
        storage.expect(requestTo("http://test-storage/upload")).andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.fileContent").value(base64))
            .andExpect(jsonPath("$.organizationId").value(1))
            .andExpect(jsonPath("$.purpose").value("risk-response-treatment"))
            .andRespond(withSuccess(document, MediaType.APPLICATION_JSON));
        storage.expect(requestTo("http://test-storage/download/" + documentId)).andRespond(withSuccess(document, MediaType.APPLICATION_JSON));
        storage.expect(requestTo("http://test-storage/download/" + documentId)).andRespond(withSuccess(document, MediaType.APPLICATION_JSON));
        OrganizationContext.setOrganization(org);
        try {
            var uploaded = service.uploadEvidence(4L, new MockMultipartFile("file", "evidence.pdf", "application/pdf", bytes), "Evidence", "risk-response-treatment");
            assertThat(uploaded.getSupportingEvidenceDocument()).isEqualTo(documentId);
            assertThat(treatment.getSupportingEvidenceDocument()).isEqualTo(documentId);
            Map<?, ?> metadata = (Map<?, ?>) service.getEvidence(4L);
            assertThat(metadata.get("fileName")).isEqualTo("evidence.pdf");
            Map<?, ?> downloaded = (Map<?, ?>) service.downloadEvidence(documentId.toString());
            assertThat(Base64.getDecoder().decode((String) downloaded.get("fileContent"))).isEqualTo(bytes);
            storage.verify();
        } finally { OrganizationContext.clear(); }
    }
}
