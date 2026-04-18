package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompanyAdminDashboardDto {
	
	long totalBranches;
	long totalDepartments;
	long totalRisks;
	long totalSubRisks;
	private List<List<CustomResponse>> topRisks = new ArrayList<>();
	public CompanyAdminDashboardDto(long totalBranches, long totalDepartments, long totalRisks, long totalSubRisks,
			List<List<CustomResponse>> topRisks) {
		super();
		this.totalBranches = totalBranches;
		this.totalDepartments = totalDepartments;
		this.totalRisks = totalRisks;
		this.totalSubRisks = totalSubRisks;
		this.topRisks = topRisks;
	}

	
	
}
