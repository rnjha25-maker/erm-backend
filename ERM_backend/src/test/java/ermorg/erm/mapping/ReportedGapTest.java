package ermorg.erm.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.constant.RiskValueUnit;
import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IUserService;
import ermorg.erm.serviceimpl.RiskService;

class ReportedGapTest {
    private final FieldMapperUtils utils = new FieldMapperUtils(mock(IUserService.class), mock(DepartmentRepository.class));

    @Test void residualScoreFallbackHonorsAllBandBoundariesAndDoesNotInventMissingData() {
        double[] scores = {1, 50, 51, 100, 101, 150, 151, 200, 201};
        String[] expected = {"Very Low", "Very Low", "Low", "Low", "Medium", "Medium", "High", "High", "Critical"};
        for (int i = 0; i < scores.length; i++) {
            var dto = new RiskReviewResponseDtoResponse();
            dto.setResidualRiskScoreRange(String.valueOf(scores[i]));
            dto.resolve(utils);
            assertThat(dto.getResidualRiskRating()).isEqualTo(expected[i]);
            assertThat(dto.getResidualRiskRatingCriteria()).isEqualTo(expected[i]);
        }
        for (String invalid : new String[]{null, "", "0", "-1", "NaN", "Infinity", "bad"})
            assertThat(utils.resolveResidualRating(null, invalid)).isNull();
        assertThat(utils.resolveResidualRating("4", "20")).isEqualTo("High");
    }

    @Test void kpaValueUnitAndMeasurementRemainDistinctInGrid() {
        var dto = new KpaKpiReviewResponseDTO();
        dto.setValueUnit(RiskValueUnit.CRORES);
        dto.setUnitOfMeasurement("Ratios");
        var unit = new CustomFieldConfig(); unit.setFieldName("Value Unit"); unit.setSystemFieldName("valueUnit");
        var measurement = new CustomFieldConfig(); measurement.setFieldName("Unit of Measurement"); measurement.setSystemFieldName("unitOfMeasurement");
        var mapper = new GenericFieldMapper(List.of(new KpaKpiReviewStrategyConfig(utils)));
        assertThat(mapper.mapFields(dto, List.of(unit, measurement), ModuleType.KPA_KPI_REVIEW))
            .containsEntry("valueUnit", "Crores").containsEntry("unitOfMeasurement", "Ratios");
    }

    @Test void legacyZeroVerticalIsNotDisplayedAsAnIdOrLookedUp() {
        assertThat((String) ReflectionTestUtils.invokeMethod(new RiskService(), "resolveBusinessVerticalName", 0L))
            .isEqualTo("Not specified");
    }

    @Test void missingVerticalNameIsExplicitAndNewZeroIdsAreRejected() throws Exception {
        var service = new RiskService();
        var rest = mock(org.springframework.web.client.RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", rest);
        ReflectionTestUtils.setField(service, "commandOrganizationServiceId", "org-service");
        when(rest.getForObject("http://org-service/business-vertical/42", String.class))
            .thenThrow(new org.springframework.web.client.RestClientException("Unavailable"));
        assertThat((String) ReflectionTestUtils.invokeMethod(service, "resolveBusinessVerticalName", 42L))
            .isEqualTo("Unavailable");
        var request = new ermorg.erm.dto.riskDTO.RiskDTO();
        request.setRiskOwnerId(1L); request.setRiskChampionId(2L); request.setBranchId(3L);
        request.setBusinessVertical(0L);
        assertThatThrownBy(() -> service.addRisk(request)).hasMessage("Please select a valid business vertical.");
    }
}
