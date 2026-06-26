package ermorg.erm.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApprovalLoginTargetResponse {
	private String redirectUrl;
	private List<ApprovalResponse> pendingApprovals;
}
