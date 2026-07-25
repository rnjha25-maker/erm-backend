package ermorg.erm.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApprovalDashboardResponse {
	private List<ApprovalResponse> pending;
	private List<ApprovalResponse> upcomingDue;
	private List<ApprovalResponse> overdue;
	private List<ApprovalResponse> history;
}
