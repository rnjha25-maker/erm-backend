package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrgAdminDashboardDto {
	
	private long totalUsers;
	private long totalBranches;
	private long totalDepartments;
	private long totalActiveUsers;
	private long totalCompanies;
	
	Map<String, Long> countByStatus;
	Map<String, Long> countByOverdue;
	
	private List<List<CustomResponse>> topRisks = new ArrayList<>();


	
	public OrgAdminDashboardDto(long totalUsers, long totalBranches, long totalDepartments, long totalActiveUsers,
			Map<String, Long> countByStatus, Map<String, Long> countByOverdue, List<List<CustomResponse>> topRisks) {
		
		this.totalUsers = totalUsers;
		this.totalBranches = totalBranches;
		this.totalDepartments = totalDepartments;
		this.totalActiveUsers = totalActiveUsers;
		this.countByStatus = countByStatus;
		this.countByOverdue = countByOverdue;
		this.topRisks = topRisks;
	}

	@Data
	@AllArgsConstructor
	class UserCounts{
		private long tota;
		private long basic;
		private long advanced;
		private long enterprise;
	}
	
	@Data
	@AllArgsConstructor
	class OrganizationStatus{
		private long active;
		private long inactive;
		private long pending;
	}
	
	@Data
	@AllArgsConstructor
	class PlanDistribution{
		private long basic;
		private long advanced;
		private long enterprise;
	}
	
	class OrganizationCount{
		
	}
}
