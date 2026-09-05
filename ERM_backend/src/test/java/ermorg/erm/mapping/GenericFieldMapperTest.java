package ermorg.erm.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.model.Department;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IUserService;

class GenericFieldMapperTest {

    @Test
    void mapFields_shouldResolveDatabaseStyleSystemFieldAgainstDtoGetterWithoutStrategy() {
        GenericFieldMapper mapper = new GenericFieldMapper(Collections.emptyList());

        CustomFieldConfig config = new CustomFieldConfig();
        config.setFieldName("Key Performance Indicator");
        config.setSystemFieldName("key_performance_indicator");

        Map<String, Object> values = mapper.mapFields(
                new KpaResponse("Revenue Growth"),
                List.of(config),
                null);

        assertThat(values)
                .containsEntry("keyperformanceindicator", "Revenue Growth")
                .containsEntry("Key Performance Indicator", "Revenue Growth")
                .containsEntry("key_performance_indicator", "Revenue Growth");
    }

    @Test
    void mapFields_shouldResolveKriBusinessFunctionAndSubmittedRiskAppetiteFields() {
        IUserService userService = mock(IUserService.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        Department department = new Department();
        department.setName("AI Governance");

        when(departmentRepository.findById(14318L)).thenReturn(Optional.of(department));

        FieldMapperUtils fieldMapperUtils = new FieldMapperUtils(userService, departmentRepository);
        GenericFieldMapper mapper = new GenericFieldMapper(
                List.of(new KriKpiReviewStrategyConfig(fieldMapperUtils)));

        KriKpiReviewResponseDTO response = new KriKpiReviewResponseDTO();
        response.setBusinessFunction("14318");
        response.setDepartmentName("14318");
        response.setRiskAppetite("Risk Appetite Breached but within Risk Tolerance");
        response.setRiskToleranceStatus("Very High");

        CustomFieldConfig businessFunction = config("Business Function", "businessFunction");
        CustomFieldConfig riskAppetite = config("Risk Appetite", "riskAppetite");
        CustomFieldConfig riskToleranceStatus = config("Risk Tolerance Status", "riskToleranceStatus");

        Map<String, Object> values = mapper.mapFields(response,
                List.of(businessFunction, riskAppetite, riskToleranceStatus),
                ModuleType.KRI_KPI_REVIEW);

        assertThat(values)
                .containsEntry("businessfunction", "AI Governance")
                .containsEntry("riskappetite", "Risk Appetite Breached but within Risk Tolerance")
                .containsEntry("risktolerancestatus", "Very High");
    }

    private CustomFieldConfig config(String fieldName, String systemFieldName) {
        CustomFieldConfig config = new CustomFieldConfig();
        config.setFieldName(fieldName);
        config.setSystemFieldName(systemFieldName);
        return config;
    }

    private static class KpaResponse {
        private final String keyPerformanceIndicator;

        private KpaResponse(String keyPerformanceIndicator) {
            this.keyPerformanceIndicator = keyPerformanceIndicator;
        }

        public String getKeyPerformanceIndicator() {
            return keyPerformanceIndicator;
        }
    }
}
