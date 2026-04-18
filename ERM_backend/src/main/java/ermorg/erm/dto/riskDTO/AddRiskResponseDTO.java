package ermorg.erm.dto.riskDTO;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.model.Risk;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddRiskResponseDTO {
	private Risk risk;
	private String message;
	private ResponseStatus status;
}
