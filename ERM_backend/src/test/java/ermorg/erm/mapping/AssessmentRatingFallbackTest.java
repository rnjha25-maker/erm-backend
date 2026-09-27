package ermorg.erm.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.dto.response.*;
import ermorg.erm.service.*;
import ermorg.erm.util.mapper.CustomResponseMapper;

class AssessmentRatingFallbackTest {
    private final FieldMapperUtils utils = new FieldMapperUtils(mock(IUserService.class), mock(DepartmentRepository.class));

    private RiskAssessmentResponse response() {
        var risk = new ermorg.erm.model.Risk(); risk.setId(1L);
        var assessment = new ermorg.erm.model.RiskAssessment(); assessment.setId(99932L); assessment.setRisk(risk);
        return new RiskAssessmentResponse(assessment);
    }

    private CustomFieldResponse field(String label, String key) {
        var f = new CustomFieldResponse(); f.setFieldName(label); f.setSystemFieldName(key);
        f.setFieldType("Dropdown"); f.setShowGridColumn(true); return f;
    }

    @Test void liveMetadataRendersAllWeightsForNullEmptyAndWhitespaceRatingsInGridAndView() throws Exception {
        var fields = mock(IFieldService.class);
        when(fields.getCustomFieldResponse(1L, "riskAssessment")).thenReturn(List.of(
            field("Inherent Risk Rating", "residualRiskRatingCriteria"),
            field("Inherent Risk Rating Score", "riskRatingScore"),
            field("Risk Rating", "riskRating")));
        var mapper = new CustomResponseMapper();
        ReflectionTestUtils.setField(mapper, "fieldService", fields);
        ReflectionTestUtils.setField(mapper, "fieldMapperUtils", utils);
        ReflectionTestUtils.setField(mapper, "genericFieldMapper", new GenericFieldMapper(List.of(new RiskAssessmentStrategyConfig(utils))));
        String[] labels = {"Very Low", "Low", "Medium", "High", "Critical"};
        for (String missing : new String[]{null, "", "  "}) {
            for (long weight = 1; weight <= 5; weight++) {
                for (boolean grid : new boolean[]{true, false}) {
                    var dto = response(); dto.setRiskRating(missing);
                    dto.setResidualRiskRatingCriteria(weight); dto.setRiskRatingScore(190.0);
                    var result = mapper.map("riskAssessment", 1L, dto, grid);
                    assertThat(result.get(0).getValue()).isEqualTo(labels[(int) weight - 1]);
                    assertThat(result.get(1).getValue()).isEqualTo("190.0");
                    assertThat(result.get(2).getValue()).isEqualTo(labels[(int) weight - 1]);
                    dto.resolve(utils);
                    assertThat(dto.getRiskRating()).isEqualTo(labels[(int) weight - 1]);
                    assertThat(dto.getResidualRiskRatingCriteria()).isEqualTo(weight);
                    assertThat(dto.getRiskRatingScore()).isEqualTo(190.0);
                }
            }
        }
    }

    @Test void explicitRatingsRemainAuthoritativeAndInvalidFallbackDoesNotInventLabels() {
        assertThat(utils.resolveAssessmentRating("Low", 4L)).isEqualTo("Low");
        assertThat(utils.resolveAssessmentRating("3", 4L)).isEqualTo("Medium");
        assertThat(utils.resolveAssessmentRating("Moderate", 4L)).isEqualTo("Moderate");
        var mapper = new GenericFieldMapper(List.of(new RiskAssessmentStrategyConfig(utils)));
        var config = new CustomFieldConfig(field("Inherent Risk Rating", "residualRiskRatingCriteria"));
        for (Long invalid : new Long[]{null, 0L, -1L, 6L, 190L}) {
            var dto = response(); dto.setResidualRiskRatingCriteria(invalid);
            assertThat(mapper.mapFields(dto, List.of(config), ModuleType.RISK_ASSESSMENT))
                .containsEntry("Inherent Risk Rating", "");
        }
    }

    @Test void serviceAddedRatingUsesSameFallbackWhenMetadataOmitsRating() {
        var service = new ermorg.erm.serviceimpl.RiskService();
        var mapper = mock(CustomResponseMapper.class);
        ReflectionTestUtils.setField(service, "customResponseMapper", mapper);
        ReflectionTestUtils.setField(service, "fieldMapperUtils", utils);
        when(mapper.map(eq("riskAssessment"), eq(1L), any(), eq(false))).thenReturn(List.of());
        var risk = new ermorg.erm.model.Risk(); risk.setId(1L);
        var assessment = new ermorg.erm.model.RiskAssessment(); assessment.setId(99932L);
        assessment.setRisk(risk); assessment.setResidualRiskRatingCriteria(4L); assessment.setRiskRatingScore(190.0);
        List<CustomResponse> result = ReflectionTestUtils.invokeMethod(service, "mapRiskAssessment", assessment);
        assertThat(result).anySatisfy(f -> {
            assertThat(f.getFieldName()).isEqualTo("Risk Rating");
            assertThat(f.getValue()).isEqualTo("High");
        });
        assertThat(assessment.getRiskRating()).isNull();
        assertThat(assessment.getResidualRiskRatingCriteria()).isEqualTo(4L);
    }
}
