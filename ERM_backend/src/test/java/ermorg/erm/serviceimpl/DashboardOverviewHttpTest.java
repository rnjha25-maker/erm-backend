package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.*;
import com.fasterxml.jackson.databind.*;
import ermorg.erm.controller.DashboardOverviewController;
import ermorg.erm.config.PeriodWorkflowOpenApiConfiguration;
import ermorg.erm.dashboard.*;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.repository.DashboardOverviewRepository;

/** Real HTTP and OpenAPI, isolated from database and production configuration. */
@SpringBootTest(classes=DashboardOverviewHttpTest.Config.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties="spring.config.location=optional:classpath:/overview-http-test-only.properties")
class DashboardOverviewHttpTest {
    @LocalServerPort int port;
    @Configuration
    @Import({DashboardOverviewController.class,DashboardOverviewService.class,ErmRiskHeatmapBuilder.class,
        PeriodWorkflowOpenApiConfiguration.class,SpringDocConfiguration.class,SpringDocWebMvcConfiguration.class,MultipleOpenApiSupportConfiguration.class})
    @EnableConfigurationProperties(SpringDocConfigProperties.class)
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class,DispatcherServletAutoConfiguration.class,
        WebMvcAutoConfiguration.class,JacksonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class})
    static class Config {
        @Bean FieldMapperUtils mapper(){return new FieldMapperUtils(null,null);}
        @Bean DashboardOverviewRepository repository(){return mock(DashboardOverviewRepository.class);}
        @Bean ChartScope.Resolver scopes() throws Exception {
            var resolver=mock(ChartScope.Resolver.class);
            when(resolver.resolve(null,null,null)).thenReturn(new ChartScope(10L,20L,null,null,null,false,null,null));
            return resolver;
        }
        @Bean DashboardChartService charts() {
            var charts=mock(DashboardChartService.class);
            when(charts.performanceForScope(any(),any(),any(),any(),anyInt(),anyInt())).thenAnswer(call->{
                ChartPeriod p=call.getArgument(0);ChartPeriod.Frequency frequency=call.getArgument(2);
                return new DashboardChartService.PerformanceResponse(p,call.getArgument(1),frequency,"EXPLICIT",p.labels(frequency),0,20,false,List.of());});
            return charts;
        }
        @Bean org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory webServer(){
            var factory=new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();
            factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");return factory;
        }
    }
    String url(String path){return "http://127.0.0.1:"+port+path;}
    @Test void realGetReturnsUiKeysAndStableEmptyShapes() throws Exception {
        var response=new RestTemplate().getForEntity(url("/dashboard/overview?year=2026"),String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        var body=new ObjectMapper().readTree(response.getBody());assertThat(body.path("status").asText()).isEqualTo("SUCCESS");
        var data=body.path("data");
        for(String key:List.of("riskLevelSummary","riskByCategory","riskBySource","riskByImpact","treatmentStrategy","assessmentType",
            "riskAppetiteStatus","ratingWiseStatus","riskRatingByLocation","riskRatingByCategory","functionWiseRiskRating","riskRatingByOwner",
            "financialExposureByRisk","ermMaturityFunctionWise","riskRegister","performance"))assertThat(data.path(key).isArray()).as(key).isTrue();
        assertThat(data.path("riskHeatmap").path("cells")).hasSize(25);
        assertThat(data.path("overdueEvaluations").isNull()).isTrue();
    }
    @Test void rejectsUnsupportedFrequencyAndPagination() {
        for(String suffix:List.of("&frequency=YEARLY","&size=101","&periodType=INVALID","&yearType=INVALID")) {
            assertThatThrownBy(()->new RestTemplate().getForEntity(url("/dashboard/overview?year=2026"+suffix),String.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
        }
    }
    @Test void swaggerPublishesCombinedEndpointAndResponseSchema() throws Exception {
        var spec=new ObjectMapper().readTree(new RestTemplate().getForObject(url("/v3/api-docs/period-workflow"),String.class));
        assertThat(spec.path("paths").has("/dashboard/overview")).isTrue();
        assertThat(spec.path("components").path("schemas").toString()).contains("riskLevelSummary","performanceSeries","unavailableMetrics","riskRegisterPage");
    }
}
