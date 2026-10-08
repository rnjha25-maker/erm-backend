package ermorg.erm.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.*;
import ermorg.erm.dashboard.DashboardChartService;
import ermorg.erm.evaluation.PeriodEvaluationService;
import ermorg.erm.config.PeriodWorkflowOpenApiConfiguration;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;

/** Generate real OpenAPI over loopback HTTP. No application/DB/storage configuration loaded. */
@SpringBootTest(classes=PeriodWorkflowOpenApiTest.Config.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties={"spring.config.location=optional:classpath:/swagger-verification-only.properties"})
class PeriodWorkflowOpenApiTest {
    @LocalServerPort int port;
    @Configuration
    @Import({PeriodEvaluationController.class,DashboardChartController.class,GlobalExceptionHandler.class,
        PeriodWorkflowOpenApiConfiguration.class,SpringDocConfiguration.class,SpringDocWebMvcConfiguration.class,MultipleOpenApiSupportConfiguration.class})
    @EnableConfigurationProperties(SpringDocConfigProperties.class)
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class,DispatcherServletAutoConfiguration.class,
        WebMvcAutoConfiguration.class,MultipartAutoConfiguration.class,JacksonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class})
    static class Config {
        @Bean PeriodEvaluationService periods(){return mock(PeriodEvaluationService.class);}
        @Bean ermorg.erm.evaluation.PeriodEvidenceClient evidence(){return mock(ermorg.erm.evaluation.PeriodEvidenceClient.class);}
        @Bean DashboardChartService charts(){return mock(DashboardChartService.class);}
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer(){
            var factory=new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");return factory;
        }
    }
    @Test void allNewEndpointsAndMultipartArePublished() throws Exception {
        var client=new org.springframework.web.client.RestTemplate();var json=new ObjectMapper();
        var response=client.getForEntity("http://127.0.0.1:"+port+"/v3/api-docs/period-workflow",String.class);
        assertThat(response.getStatusCode().value()).withFailMessage(response.getBody()).isEqualTo(200);
        JsonNode spec=json.readTree(response.getBody());var paths=spec.path("paths");
        for(String path:new String[]{"/period-evaluations/start","/period-evaluations","/period-evaluations/{id}","/period-evaluations/{id}/complete","/period-evaluations/{id}/evidence","/period-evaluations/{id}/evidence/{documentId}","/dashboard/charts/performance","/dashboard/charts/exposure","/dashboard/charts/trend","/dashboard/charts/scatter"})
            assertThat(paths.has(path)).as(path).isTrue();
        assertThat(paths.path("/period-evaluations/{id}").has("patch")).isTrue();
        assertThat(paths.path("/period-evaluations/{id}/evidence").path("post").path("requestBody").path("content").has("multipart/form-data")).isTrue();
        assertThat(spec.path("components").path("schemas").toString()).contains("periodNumber","effectiveDate","revision","QUARTERLY","FINANCIAL");
        assertThat(spec.path("servers").get(0).path("url").asText()).isEqualTo("/erm");
        Files.writeString(Path.of("target/period-workflow-openapi.json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(spec));
        var all=client.getForEntity("http://127.0.0.1:"+port+"/v3/api-docs",String.class);
        assertThat(all.getStatusCode().value()).isEqualTo(200);
        assertThat(json.readTree(all.getBody()).path("paths").has("/period-evaluations/start")).isTrue();
    }
}
