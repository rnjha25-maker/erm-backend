package ermorg.erm.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmMaturitySummaryGroup {

	private String ermMaturityId;
	private BigDecimal totalWeightageScore;
	private String overallMaturityLevel;
	private String displayLabel;
	private Long companyId;
	private List<Long> departmentIds = new ArrayList<>();
}
