package ermorg.erm.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;
import ermorg.erm.dto.response.*;
import ermorg.erm.service.IDashboardService;

/** Actual MVC routing/JSON over HTTP, with no shared database or authentication calls. */
@SpringBootTest(classes=DashboardRouteHttpTest.Config.class,
    webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties={"spring.config.location=optional:classpath:/dashboard-route-verification.properties"})
class DashboardRouteHttpTest {
    @LocalServerPort int port;
    private final RestTemplate client = new RestTemplate();

    @Configuration
    @Import({DashboardController.class, GlobalExceptionHandler.class})
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class,
        JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    static class Config {
        @Bean IDashboardService dashboard() throws Exception {
            var service = mock(IDashboardService.class);
            when(service.getErmDashboardSummaryV2(2026, ErmDashboardPeriodType.YEAR, 14314L, null, 0L))
                .thenReturn(new ErmDashboardSummaryV2Response());
            when(service.getErmRiskRegister(2026, ErmDashboardPeriodType.YEAR, 14314L, null, 0L, 0, 20))
                .thenReturn(RiskRegisterPage.of(java.util.List.of(), 0, 20, 0));
            when(service.getErmDashboardSummary(2026, ErmDashboardPeriodType.YEAR, 14314L, null, 0L))
                .thenReturn(new ErmDashboardSummaryResponse());
            return service;
        }
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer() {
            var server = new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();
            server.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
            return server;
        }
    }

    private String url(String path) { return "http://127.0.0.1:" + port + path; }

    @Test void reportedSummaryV2RequestResolves() {
        var response = client.getForEntity(url("/dashboard/erm-summary-v2?year=2026&periodType=YEAR&companyId=14314&functionId=0&page=0&size=10"), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"SUCCESS\"", "\"riskHeatmap\"");
    }

    @Test void reportedRiskRegisterRequestResolvesAndPreservesPagination() {
        var response = client.getForEntity(url("/dashboard/risk-register?year=2026&periodType=YEAR&companyId=14314&functionId=0&page=0&size=20"), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"SUCCESS\"", "\"page\":0", "\"size\":20");
    }

    @Test void originalSummaryRouteStillResolves() {
        var response = client.getForEntity(url("/dashboard/erm-summary?year=2026&periodType=YEAR&companyId=14314&functionId=0"), String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"SUCCESS\"");
    }

    @Test void missingRouteReturns404InsteadOfMisleading500() {
        assertThatThrownBy(() -> client.getForEntity(url("/dashboard/nonexistent-route"), String.class))
            .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
