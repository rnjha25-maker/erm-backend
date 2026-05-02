package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmDashboardSummaryResponse {

	private long totalRisks;
	private List<NamedCount> byCategory = new ArrayList<>();
	private ErmHierarchyBreakdown hierarchy = new ErmHierarchyBreakdown();
	private List<NamedCount> byPriority = new ArrayList<>();
	private List<NamedCount> byTreatmentStrategy = new ArrayList<>();
	private List<NamedCount> byImpact = new ArrayList<>();
	private List<NamedCount> byRating = new ArrayList<>();
	private List<NamedCount> byAnalysisType = new ArrayList<>();
	private List<NamedCount> byFinancialExposure = new ArrayList<>();
	private List<NamedCount> bySource = new ArrayList<>();
}
