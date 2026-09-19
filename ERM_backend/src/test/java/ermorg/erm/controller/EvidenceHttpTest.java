package ermorg.erm.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.model.*;
import ermorg.erm.service.IRiskTreatmentService;

// Real HTTP + servlet multipart parsing. Service mocked deliberately: no shared DB/S3 access.
@SpringBootTest(classes = EvidenceHttpTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=upload-test"})
class EvidenceHttpTest {
    @LocalServerPort int port;
    @Configuration
    @Import({RiskTreatmentController.class, GlobalExceptionHandler.class})
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
        WebMvcAutoConfiguration.class, MultipartAutoConfiguration.class, JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class})
    static class Config {
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer() {
            var factory = new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();
            factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
            return factory;
        }
        @Bean IRiskTreatmentService service() throws Exception {
            var service = mock(IRiskTreatmentService.class);
            when(service.uploadEvidence(eq(42L), any(), any(), eq("risk-response-treatment"))).thenAnswer(i -> {
                MultipartFile file = i.getArgument(1);
                assertThat(file.getContentType()).isEqualTo("application/pdf");
                assertThat(new String(file.getBytes(), 0, 8, StandardCharsets.US_ASCII)).startsWith("%PDF-");
                var risk = new Risk(); risk.setId(1L);
                var user = new User(); user.setId(2L);
                var treatment = new RiskResponseTreatment(); treatment.setId(42L);
                treatment.setRisk(risk); treatment.setRiskReporting(user);
                treatment.setSupportingEvidenceDocument(UUID.fromString("00000000-0000-0000-0000-000000000042"));
                return new RiskResponseTreatmentResponse(treatment);
            });
            return service;
        }
    }

    record Result(int statusCode, String body) {}
    static String pdf(int padding) {
        StringBuilder pdfBuilder = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[3];
        String[] objects = {"<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << >> >>"};
        for (int i = 0; i < objects.length; i++) {
            offsets[i] = pdfBuilder.length();
            pdfBuilder.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        pdfBuilder.append('%').append("x".repeat(padding)).append('\n');
        int xref = pdfBuilder.length();
        pdfBuilder.append("xref\n0 4\n0000000000 65535 f \n");
        for (int offset : offsets) pdfBuilder.append(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offset));
        pdfBuilder.append("trailer\n<< /Root 1 0 R /Size 4 >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        return pdfBuilder.toString();
    }

    private Result upload(int padding) throws Exception {
        String pdf = pdf(padding);
        String boundary = "EvidenceBoundary";
        String body = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"purpose\"\r\n\r\nrisk-response-treatment\r\n"
            + "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"evidence.pdf\"\r\nContent-Type: application/pdf\r\n\r\n"
            + pdf + "\r\n--" + boundary + "--\r\n";
        var connection = (java.net.HttpURLConnection) URI.create("http://127.0.0.1:" + port + "/risk-treatment/42/evidence").toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);
        try (var out = connection.getOutputStream()) { out.write(body.getBytes(StandardCharsets.UTF_8)); }
        int status = connection.getResponseCode();
        Result response;
        try (var in = status < 400 ? connection.getInputStream() : connection.getErrorStream()) {
            response = new Result(status, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } finally { connection.disconnect(); }
        System.out.println("PDF bytes=" + pdf.length() + " HTTP " + response.statusCode() + " " + response.body());
        return response;
    }

    @Test void acceptsPdfLargerThanDefaultOneMegabyte() throws Exception {
        var response = upload(1200000);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("SUCCESS", "supportingEvidenceDocument");
    }
    @Test void rejectsPdfAboveTenMegabytesWithUsefulError() throws Exception {
        var response = upload(11 * 1024 * 1024);
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(response.body()).contains("10MB");
    }
}
