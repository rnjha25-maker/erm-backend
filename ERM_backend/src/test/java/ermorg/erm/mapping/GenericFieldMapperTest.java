package ermorg.erm.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
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

    @Test
    void mapFields_shouldResolveKpaReviewUsingKriStyleAliases() {
        IUserService userService = mock(IUserService.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        Department department = new Department();
        department.setName("Operations");

        when(departmentRepository.findById(42L)).thenReturn(Optional.of(department));

        FieldMapperUtils fieldMapperUtils = new FieldMapperUtils(userService, departmentRepository);
        GenericFieldMapper mapper = new GenericFieldMapper(
                List.of(new KpaKpiReviewStrategyConfig(fieldMapperUtils)));

        KpaKpiReviewResponseDTO response = new KpaKpiReviewResponseDTO();
        response.setRiskTitle("Disaster Management Plan");
        response.setRiskSubTitle("Risk of Natural Disaster");
        response.setDepartmentName("42");
        response.setOwnerName("Karan Gupta");
        response.setKeyRiskIndicator("Revenue Growth");
        response.setTypesOfKeyRiskIndicator("Lagging KPI");
        response.setRiskAcceptanceLevel(RiskAcceptanceLevel.ACCEPTABLE_RISK);
        response.setRiskAppetiteLevel(RiskAcceptanceLevel.ACCEPTABLE_RISK.name());
        response.setRiskAppetiteStatus("Within Appetite");
        response.setTargets("100");
        response.setKpiEvaluationFrequency("QUARTERLY");
        response.setEvaluationByName("Monu Verma");
        response.setReportingName("Patel Patel");

        Map<String, Object> values = mapper.mapFields(response,
                List.of(
                        config("Risk Title", "riskTitle"),
                        config("Risk Sub Title", "riskSubTitle"),
                        config("Department", "department"),
                        config("Risk Owner", "riskOwner"),
                        config("Key Risk Indicator", "keyRiskIndicator"),
                        config("Types of Key Risk Indicator", "typesOfKeyRiskIndicator"),
                        config("Risk Appetite Level", "riskAppetiteLevel"),
                        config("Risk Appetite Status", "riskAppetiteStatus"),
                        config("Target", "targets"),
                        config("KRI Evaluation Frequency", "kriEvaluationFrequency"),
                        config("Key Risk Evaluation by", "keyRiskEvaluationBy"),
                        config("Reporting", "reporting")),
                ModuleType.KPA_KPI_REVIEW);

        assertThat(values)
                .containsEntry("risktitle", "Disaster Management Plan")
                .containsEntry("risksubtitle", "Risk of Natural Disaster")
                .containsEntry("department", "Operations")
                .containsEntry("riskowner", "Karan Gupta")
                .containsEntry("keyriskindicator", "Revenue Growth")
                .containsEntry("typesofkeyriskindicator", "Lagging KPI")
                .containsEntry("riskappetitelevel", "ACCEPTABLE_RISK")
                .containsEntry("riskappetitestatus", "Within Appetite")
                .containsEntry("targets", "100")
                .containsEntry("krievaluationfrequency", "QUARTERLY")
                .containsEntry("keyriskevaluationby", "Monu Verma")
                .containsEntry("reporting", "Patel Patel");
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
