package ermorg.erm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErmFinancialExposureRow {

	private Long riskId;
	private String riskTitle;
	private Double annualLossExpectancy;
	private String currency;
}
