package ermorg.erm.dto.riskDTO;

import java.util.List;

import lombok.Data;

@Data
public class ErmMaturityRequest {

	private Long companyId;

	private List<ErmMaturityDto> maturityRequest;
}

