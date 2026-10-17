package ermorg.erm.config;

import org.springframework.context.annotation.*;
import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

@Configuration
public class PeriodWorkflowOpenApiConfiguration {
    @Bean
    GroupedOpenApi periodWorkflowOpenApi() {
        return GroupedOpenApi.builder().group("period-workflow")
            .pathsToMatch("/period-evaluations/**","/dashboard/charts/**")
            .addOpenApiCustomizer(api->api.setServers(List.of(
                new Server().url("/erm").description("Through the API gateway"),
                new Server().url("/").description("Direct ERM backend; trusted internal access only"))))
            .build();
    }
}
