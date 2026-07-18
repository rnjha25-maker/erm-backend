package ermorg.erm.dto.riskDTO;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.model.RiskSubControl;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RiskSubControlDto {

	private long subControlId;
	private String controlSubTitle;
	private String riskAppetiteStatus;
	private RiskAcceptanceLevel riskAcceptanceLevel;
	
	public RiskSubControlDto(RiskSubControl riskSubControl) {
		this.subControlId = riskSubControl.getId();
		this.controlSubTitle = riskSubControl.getSubControlTitle();
		this.riskAppetiteStatus = riskSubControl.getRiskAppetiteStatus();
		this.riskAcceptanceLevel = riskSubControl.getRiskAcceptanceLevel();
	}
}
