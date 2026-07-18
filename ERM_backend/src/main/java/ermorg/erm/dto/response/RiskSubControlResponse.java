package ermorg.erm.dto.response;

import ermorg.erm.constant.RiskAcceptanceLevel;
import lombok.Data;

@Data
public class RiskSubControlResponse {
	
	private long subControlId;
	private String controlSubTitle;
	private String riskAppetiteStatus;
	private RiskAcceptanceLevel riskAcceptanceLevel;

}
