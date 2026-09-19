package ermorg.erm.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.*;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;
import ermorg.erm.model.*;
import ermorg.erm.serviceimpl.KripKpiRiskService;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

/** Opt-in only: targets the disposable loopback database, never a configured shared database. */
@EnabledIfEnvironmentVariable(named = "ERM_LOCAL_DB_TEST", matches = "true")
@SpringBootTest(classes = LocalPersistenceTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.config.location=optional:classpath:/local-verification-only.properties",
        "spring.datasource.url=jdbc:mysql://127.0.0.1:3308/erm_verification",
        "spring.datasource.username=root", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update", "spring.jpa.open-in-view=true",
        "erm.storage-service-url=http://test-storage", "spring.servlet.multipart.max-file-size=10MB",
        "spring.servlet.multipart.max-request-size=12MB"})
class LocalPersistenceTest {
    @LocalServerPort int port;
    @PersistenceContext EntityManager em;
    @Autowired PlatformTransactionManager transactions;
    @org.springframework.boot.test.mock.mockito.MockBean FieldMapperUtils fields;
    @org.springframework.boot.test.mock.mockito.MockBean CustomResponseMapper customMapper;
    @org.springframework.boot.test.mock.mockito.MockBean ermorg.erm.service.IFieldService fieldService;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("storageRestTemplate") RestTemplate storageClient;
    static volatile Organization organization;

    @Configuration
    @EntityScan("ermorg.erm.model")
    @EnableJpaRepositories("ermorg.erm.repository")
    @Import({KriKpiReviewController.class, KripKpiRiskService.class, GlobalExceptionHandler.class,
        RiskTreatmentController.class, ermorg.erm.serviceimpl.RiskTreatmentService.class})
    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class, MultipartAutoConfiguration.class,
        JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    static class Config implements WebMvcConfigurer {
        @Bean RestTemplate storageRestTemplate() { return new RestTemplate(); }
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer() {
            var server = new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();
            server.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
            return server;
        }
        @Override public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HandlerInterceptor() {
                @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                    OrganizationContext.setOrganization(organization); return true;
                }
                @Override public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception e) {
                    OrganizationContext.clear();
                }
            });
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void createUpdateAndFreshGetPersistInMySql() throws Exception {
        var tx = new TransactionTemplate(transactions);
        long[] ids = tx.execute(status -> {
            var org = new Organization(); org.setName("Disposable KRI verification"); em.persist(org); organization = org;
            var detail = new UserDetail(); detail.setFirstName("Indira"); detail.setLastName("Gupta"); em.persist(detail);
            var user = new User(); user.setEmail("local-test@example.invalid"); user.setUserDetail(detail); user.setOrganization(org); em.persist(user);
            var risk = new Risk(); risk.setRisktitle("Environmental risk"); risk.setOrganizationId(org.getId()); em.persist(risk);
            var assessment = new RiskAssessment(); assessment.setRisk(risk); assessment.setOrganization(org); em.persist(assessment);
            em.flush();
            return new long[] {risk.getId(), user.getId(), assessment.getId()};
        });
        var request = new java.util.LinkedHashMap<String, Object>();
        request.put("riskId", ids[0]); request.put("riskOwner", ids[1]); request.put("riskAssessmentId", ids[2]);
        request.put("reporting", ids[1]); request.put("kriEvaluationBy", ids[1]);
        request.put("businessObjectives", "Environmental risk"); request.put("target", "15");
        request.put("riskToleranceMin", "10"); request.put("riskToleranceMax", "20");
        request.put("unitOfMeasurement", "Ratios"); request.put("currency", "GBP"); request.put("valueUnit", "Crores");
        request.put("reportingFrequency", "Monthly"); request.put("kriEvaluationFrequency", "Monthly");
        request.put("january", "0"); request.put("q1", "12"); request.put("measurableParameters", "Emissions ratio");
        request.put("dueDate", "2026-09-16T17:10:05Z");
        var client = new RestTemplate();
        String base = "http://127.0.0.1:" + port + "/kri-kpi-riview";
        var created = client.postForEntity(base + "/save", request, Map.class);
        assertThat(created.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> data = (Map<String, Object>) created.getBody().get("data");
        long id = ((Number) data.get("kriId")).longValue();
        System.out.println("MYSQL KRI create HTTP " + created.getStatusCode().value() + " id=" + id);
        request.put("kriId", id); request.put("target", "19");
        var updated = client.postForEntity(base + "/save", request, Map.class);
        assertThat(updated.getBody().get("message")).isEqualTo("KRI updated successfully.");
        var fetched = client.getForEntity(base + "/" + id, Map.class);
        Map<String, Object> loaded = (Map<String, Object>) fetched.getBody().get("data");
        assertThat(loaded).containsEntry("target", "19").containsEntry("currency", "GBP")
            .containsEntry("unitOfMeasurement", "Ratios").containsEntry("valueUnit", "Crores")
            .containsEntry("riskOwnerName", "Indira Gupta").containsEntry("january", "0")
            .containsEntry("q1", "12").containsEntry("measurableParameters", "Emissions ratio")
            .containsEntry("riskToleranceMin", "10").containsEntry("riskToleranceMax", "20")
            .containsEntry("dueDate", "2026-09-16T17:10:05Z");
        tx.executeWithoutResult(status -> {
            em.clear();
            KriKpiReview row = em.find(KriKpiReview.class, id);
            assertThat(row.getTarget()).isEqualTo("19");
            assertThat(row.getRiskAssessment().getId()).isEqualTo(ids[2]);
        });
        System.out.println("MYSQL KRI update HTTP " + updated.getStatusCode().value() + " fresh GET HTTP " + fetched.getStatusCode().value() + " " + loaded);

        String treatmentBase = "http://127.0.0.1:" + port + "/risk-treatment";
        var treatmentRequest = new java.util.LinkedHashMap<String, Object>();
        treatmentRequest.put("riskId", ids[0]); treatmentRequest.put("riskReporting", ids[1]);
        var treatmentCreated = client.postForEntity(treatmentBase + "/save", treatmentRequest, Map.class);
        Map<String, Object> treatmentData = (Map<String, Object>) treatmentCreated.getBody().get("data");
        long treatmentId = ((Number) treatmentData.get("riskResponseTreatmentId")).longValue();
        byte[] pdf = EvidenceHttpTest.pdf(1200000).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        String encoded = java.util.Base64.getEncoder().encodeToString(pdf);
        String documentId = java.util.UUID.randomUUID().toString();
        String document = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of("status", "SUCCESS", "data", Map.of(
            "documentId", documentId, "fileContent", encoded, "fileName", "evidence", "fileExtension", ".pdf", "contentType", "application/pdf")));
        var storage = org.springframework.test.web.client.MockRestServiceServer.bindTo(storageClient).build();
        storage.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://test-storage/upload"))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.fileContent").value(encoded))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(document, MediaType.APPLICATION_JSON));
        storage.expect(org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://test-storage/download/" + documentId))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(document, MediaType.APPLICATION_JSON));
        var form = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        var fileHeaders = new HttpHeaders(); fileHeaders.setContentType(MediaType.APPLICATION_PDF);
        form.add("file", new HttpEntity<>(new org.springframework.core.io.ByteArrayResource(pdf) {
            @Override public String getFilename() { return "evidence.pdf"; }
        }, fileHeaders));
        form.add("purpose", "risk-response-treatment");
        var uploaded = client.postForEntity(treatmentBase + "/" + treatmentId + "/evidence", form, Map.class);
        assertThat(uploaded.getStatusCode().value()).isEqualTo(200);
        treatmentRequest.put("riskResponseTreatmentId", treatmentId);
        client.postForEntity(treatmentBase + "/save", treatmentRequest, Map.class);
        var reloaded = client.getForEntity(treatmentBase + "/" + treatmentId, Map.class);
        assertThat((Map<String, Object>) reloaded.getBody().get("data")).containsEntry("supportingEvidenceDocument", documentId);
        tx.executeWithoutResult(status -> {
            em.clear();
            assertThat(em.find(RiskResponseTreatment.class, treatmentId).getSupportingEvidenceDocument().toString()).isEqualTo(documentId);
        });
        var downloaded = client.getForEntity(treatmentBase + "/evidence/" + documentId, Map.class);
        Map<String, Object> downloadedData = (Map<String, Object>) downloaded.getBody().get("data");
        assertThat(java.util.Base64.getDecoder().decode((String) downloadedData.get("fileContent"))).isEqualTo(pdf);
        java.nio.file.Files.write(java.nio.file.Path.of("target/local-evidence-downloaded.pdf"), pdf);
        storage.verify();
        System.out.println("MYSQL evidence upload HTTP 200; treatment save then GET retained UUID=" + documentId + "; download HTTP 200 byte-equal PDF=" + pdf.length);
    }
}
