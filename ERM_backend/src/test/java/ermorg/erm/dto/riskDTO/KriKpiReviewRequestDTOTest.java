package ermorg.erm.dto.riskDTO;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class KriKpiReviewRequestDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsRiskSubTitleAliasToSubRiskIds() throws Exception {
        KriKpiReviewRequestDTO dto = objectMapper.readValue(
                "{\"riskSubTitle\":\"63252\"}",
                KriKpiReviewRequestDTO.class);

        assertThat(dto.getSubRiskIds()).containsExactly(63252L);
    }

    @Test
    void mapsLegacyRiskSubsAliasToSubRiskIds() throws Exception {
        KriKpiReviewRequestDTO dto = objectMapper.readValue(
                "{\"riskSubs\":[63252,63253]}",
                KriKpiReviewRequestDTO.class);

        assertThat(dto.getSubRiskIds()).containsExactly(63252L, 63253L);
    }

    @Test
    void mapsDisplayFieldAliasesToKriPayloadFields() throws Exception {
        KriKpiReviewRequestDTO dto = objectMapper.readValue(
                """
                {
                  "keyRiskIndicator": "System Downtime",
                  "typesOfKeyRiskIndicator": "Financial KRI",
                  "riskToleranceMin": "3",
                  "riskToleranceMax": "5"
                }
                """,
                KriKpiReviewRequestDTO.class);

        assertThat(dto.getKeyRiskIndicatorKri()).isEqualTo("System Downtime");
        assertThat(dto.getTypesOfKeyRiskIndicatorKri()).isEqualTo("Financial KRI");
        assertThat(dto.getRiskToleranceRangeMinValue()).isEqualTo("3");
        assertThat(dto.getRiskToleranceRangeMaxValue()).isEqualTo("5");
    }
}
