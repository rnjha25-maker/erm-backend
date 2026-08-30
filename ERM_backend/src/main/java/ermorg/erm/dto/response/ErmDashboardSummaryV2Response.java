package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmDashboardSummaryV2Response {

	/** Risk count grouped by risk category. */
	private List<NamedCount> ermRiskSummary = new ArrayList<>();

	/** Risk count grouped by risk register type. */
	private List<NamedCount> riskSummary = new ArrayList<>();

	/** Revised impact score bucketed into Catastrophic..Insignificant. */
	private List<NamedCount> riskSummaryByImpact = new ArrayList<>();

	/** Risk count grouped by residual risk rating. */
	private List<NamedCount> riskRatingStatusOverview = new ArrayList<>();

	/** Location (branch) against residual risk rating. */
	private List<ErmGroupBreakdown> riskRatingByLocation = new ArrayList<>();

	/** Department against residual risk rating. */
	private List<ErmGroupBreakdown> functionWiseRiskRatingSummary = new ArrayList<>();

	/** Risk category against residual risk rating. */
	private List<ErmGroupBreakdown> riskRatingBycategory = new ArrayList<>();

	/** Risk owner against residual risk rating. */
	private List<ErmGroupBreakdown> riskRatingByOwner = new ArrayList<>();

	/** Company against residual risk rating; total is the company's full risk count. */
	private List<ErmGroupBreakdown> riskRatingByGroupCompany = new ArrayList<>();

	/** Quantitative risks only: risk title with annual loss expectancy. */
	private List<ErmFinancialExposureRow> financialExposureByRisk = new ArrayList<>();

	/** Total number of risks in scope. */
	private long highRiskKris;

	/** Number of risks rated Critical. */
	private long totalKris;

	private long withinRiskAppetite;

	private long riskToleranceBreached;

	/** Number of acceptable risks (legacy key, relabelled in the UI). */
	private long overdueEvaluations;

	/** Number of unacceptable risks (legacy key, relabelled in the UI). */
	private long upcomingReview;

	/** Raw stored appetite status values behind withinRiskAppetite/riskToleranceBreached. */
	private List<NamedCount> riskAppetiteStatusCounts = new ArrayList<>();

	/** Raw stored acceptance level values behind overdueEvaluations/upcomingReview. */
	private List<NamedCount> riskAcceptanceLevelCounts = new ArrayList<>();
}
