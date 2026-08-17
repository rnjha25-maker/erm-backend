package ermorg.erm.dto.riskDTO;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RiskControlDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void mapsLegacyRiskSubsStringToSubRiskIds() throws Exception {
		RiskControlDto dto = objectMapper.readValue("{\"riskSubs\":\"63252\"}", RiskControlDto.class);

		assertThat(dto.getSubRiskIds()).containsExactly(63252L);
	}

	@Test
	void mapsCanonicalSubRiskIdsArray() throws Exception {
		RiskControlDto dto = objectMapper.readValue("{\"subRiskIds\":[63252]}", RiskControlDto.class);

		assertThat(dto.getSubRiskIds()).containsExactly(63252L);
	}

	@Test
	void preservesEmptyAndInvalidIdPayloadsForServiceValidation() throws Exception {
		RiskControlDto empty = objectMapper.readValue("{\"subRiskIds\":[]}", RiskControlDto.class);
		RiskControlDto invalid = objectMapper.readValue("{\"subRiskIds\":[0]}", RiskControlDto.class);

		assertThat(empty.getSubRiskIds()).isEmpty();
		assertThat(invalid.getSubRiskIds()).containsExactly(0L);
	}
}
