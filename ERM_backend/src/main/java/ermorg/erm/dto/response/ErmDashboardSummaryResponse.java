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

	/** Org admin only; null for company/advance/basic users. */
	private List<ErmRatingHierarchyGroup> byRatingHierarchy;

	/** Org admin only; null for company/advance/basic users. */
	private List<ErmCompanyCategoryGroup> byCompanyCategory;

	private List<ErmBranchRatingGroup> byBranchRating = new ArrayList<>();

	private List<ErmFunctionRatingGroup> byFunctionRating = new ArrayList<>();

	private List<ErmCategoryBranchGroup> byCategoryBranch = new ArrayList<>();

	private List<ErmOwnerRatingGroup> byOwnerRating = new ArrayList<>();

	private List<ErmMaturitySummaryGroup> ermMaturityCompanyWise = new ArrayList<>();

	private List<ErmMaturitySummaryGroup> ermMaturityFunctionWise = new ArrayList<>();

	private RiskRegisterPage riskRegister = new RiskRegisterPage();
}
