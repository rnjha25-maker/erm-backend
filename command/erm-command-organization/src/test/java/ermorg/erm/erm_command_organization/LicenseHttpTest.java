package ermorg.erm.erm_command_organization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import ermorg.erm.erm_command_organization.controller.LicenseController;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.repository.*;
import ermorg.erm.erm_command_organization.serviceimpl.LicenseServiceImpl;

/** Real loopback HTTP, real controller/service, no database writes or gateway bypass test. */
@SpringBootTest(classes=LicenseHttpTest.Config.class, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties="spring.config.location=optional:classpath:/license-http-test-only.properties")
class LicenseHttpTest {
    @LocalServerPort int port;
    @Configuration
    @Import({LicenseController.class, LicenseServiceImpl.class})
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
        WebMvcAutoConfiguration.class, JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    static class Config {
        @Bean OrganizationRepository organizations() {
            var repository=mock(OrganizationRepository.class);
            when(repository.findById(101137L)).thenReturn(Optional.of(new Organization()));
            return repository;
        }
        @Bean LicenseRepository licenses() {
            var repository=mock(LicenseRepository.class);
            when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
            return repository;
        }
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer() {
            var factory=new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();
            factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
            return factory;
        }
    }
    @Test void createOverHttp() throws Exception {
        var headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        String payload="""
            {"organizationId":101137,"planType":"Advance","startDate":"2026-10-18",
             "endDate":"2027-10-18","gracePeriodDays":15,"autoRenew":true}
            """;
        var response=new RestTemplate().postForEntity("http://127.0.0.1:"+port+"/license/create",
            new HttpEntity<>(payload,headers),String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body=new ObjectMapper().readTree(response.getBody());
        assertThat(body.path("status").asText()).isEqualTo("SUCCESS");
        assertThat(body.path("data").path("planType").asText()).isEqualTo("Advance");
        assertThat(body.path("data").path("gracePeriodDays").asInt()).isEqualTo(15);
        assertThat(body.path("data").path("autoRenew").asBoolean()).isTrue();
    }
}
