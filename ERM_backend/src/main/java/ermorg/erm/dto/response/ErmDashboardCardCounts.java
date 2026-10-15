package ermorg.erm.dto.response;

import lombok.Data;

@Data
public class ErmDashboardCardCounts {

	private long totalRiskCount;
	private long totalRiskAppetite;
	private long totalRiskTolerance;
	private long totalOverdue;
}
