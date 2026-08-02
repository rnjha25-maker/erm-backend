package ermorg.erm.dto.riskDTO;

import ermorg.erm.constant.ApprovalStatus;
import lombok.Data;

@Data
public class ApprovalDecisionRequest {
	private ApprovalStatus status;
	private String comment;
	private String rootCause;
	private String actionTaken;
}
