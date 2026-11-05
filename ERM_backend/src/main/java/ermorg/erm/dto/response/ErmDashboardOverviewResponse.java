package ermorg.erm.dto.response;

import java.math.BigDecimal;
import java.util.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/** UI contract v1. Null metrics are unavailable, never substituted with unrelated counts. */
@Data
public class ErmDashboardOverviewResponse {
    private long totalRisks;
    private long withinRiskAppetite;
    private long riskToleranceBreached;
    private Long overdueEvaluations;
    private Long upcomingReview;
    private Long totalKris;
    private Long highRiskKris;
    private List<NamedCount> riskLevelSummary = new ArrayList<>();
    private List<NamedCount> riskSummaryPriorityBased = new ArrayList<>();
    private List<NamedCount> riskByCategory = new ArrayList<>();
    private List<NamedCount> riskBySource = new ArrayList<>();
    private List<NamedCount> riskByImpact = new ArrayList<>();
    private List<NamedCount> treatmentStrategy = new ArrayList<>();
    private List<NamedCount> assessmentType = new ArrayList<>();
    private List<NamedCount> riskAppetiteStatus = new ArrayList<>();
    private List<RatingStatus> ratingWiseStatus = new ArrayList<>();
    private ErmRiskHeatmap riskHeatmap;
    private List<ErmGroupBreakdown> riskRatingByLocation = new ArrayList<>();
    private List<ErmGroupBreakdown> riskRatingByCategory = new ArrayList<>();
    private List<ErmGroupBreakdown> functionWiseRiskRating = new ArrayList<>();
    private List<ErmGroupBreakdown> riskRatingByOwner = new ArrayList<>();
    private List<ExposureRow> financialExposureByRisk = new ArrayList<>();
    private List<ErmMaturitySummaryGroup> ermMaturityFunctionWise = new ArrayList<>();
    private List<ErmMaturitySummaryGroup> ermMaturityCompanyWise = new ArrayList<>();
    private List<RegisterRow> riskRegister = new ArrayList<>();
    private PageMetadata riskRegisterPage;
    private List<PerformancePoint> performance = new ArrayList<>();
    private List<PerformanceSeries> performanceSeries = new ArrayList<>();
    private PerformancePage performancePage;
    private List<RatingCut> ratingsByGroupCompany = new ArrayList<>();
    private List<RatingCut> ratingsByCategory = new ArrayList<>();
    private List<RatingCut> ratingsByDepartment = new ArrayList<>();
    private List<NamedCount> riskByStatus = new ArrayList<>();
    private List<RiskTrendPoint> riskTrend = new ArrayList<>();
    private List<TopRisk> topRisks = new ArrayList<>();
    private List<NamedCount> riskByDepartment = new ArrayList<>();
    private List<NamedCount> reportingFrequencyDistribution = new ArrayList<>();
    private List<RatingAppetite> riskRatingAppetite = new ArrayList<>();
    private List<NamedValue> quickStats = new ArrayList<>();
    private List<NamedCount> treatmentPlanStatus = new ArrayList<>();
    private List<WorkflowCount> approvalQueue = new ArrayList<>();
    private ControlEffectiveness controlEffectiveness;
    private List<ActionItem> overdueItems = new ArrayList<>();
    private List<ActionItem> myActionItems = new ArrayList<>();
    private List<IndicatorEvaluation> kriStatusOverview = new ArrayList<>();
    private List<IndicatorEvaluation> kriEvaluation = new ArrayList<>();
    private List<IndicatorEvaluation> kriAppetiteEvaluation = new ArrayList<>();
    private List<IndicatorEvaluation> kraKpiEvaluation = new ArrayList<>();
    private List<IndicatorEvaluation> kpiPeriodical = new ArrayList<>();
    private List<ComplianceRow> functionWiseCompliance = new ArrayList<>();
    private List<DynamicCategoryCount> dynamicCategoryRecords = new ArrayList<>();
    private Map<String,String> unavailableMetrics = new LinkedHashMap<>();
    private List<String> notes = new ArrayList<>();
    public record RatingStatus(String label, @JsonProperty("Open") long open,
        @JsonProperty("In Progress") long inProgress, @JsonProperty("Closed") long closed,
        @JsonProperty("Unknown") long unknown) {}
    public record RegisterRow(Long riskId, String riskTitle, String category, String owner,
        BigDecimal likelihood, BigDecimal impact, BigDecimal inherentScore, BigDecimal residualScore,
        String rating, String status) {}
    public record ExposureRow(Long riskId, String riskTitle, BigDecimal annualLossExpectancy,
        String currency, String valueUnit, String basis) {}
    public record PageMetadata(int page, int size, long totalElements, boolean hasNext) {}
    public record PerformancePage(int page, int size, boolean hasNext) {}
    public record PerformancePoint(String period, BigDecimal actual, BigDecimal target) {}
    public record PerformanceSeries(long recordId, String indicator, String currency, String valueUnit,
        String measurementUnit, List<PerformancePoint> points, List<String> invalidFields) {}
    public record RatingCut(String key, String displayLabel, long total, List<NamedCount> breakdown) {}
    public record RiskTrendPoint(String key, String displayLabel, long cumulativeValue) {}
    public record TopRisk(Long riskId, String code, String riskTitle, String owner, BigDecimal score,
        String band, String treatmentStatus) {}
    public record RatingAppetite(Long riskId, String inherent, String residual, String appetite,
        String toleranceStatus, String acceptanceStatus) {}
    public record NamedValue(String key, String displayLabel, long value) {}
    public record WorkflowCount(String workflowType, long count, long overdue) {}
    public record ControlEffectiveness(BigDecimal averageScore, long assessmentCount,
        List<NamedCount> byRating) {}
    public record ActionItem(Long id, String type, String displayLabel, String due,
        String priority, Integer progress, Long recordId) {}
    public record IndicatorEvaluation(Long id, Long riskId, String indicator, String source, String uom,
        BigDecimal threshold, BigDecimal current, BigDecimal previous, String status,
        BigDecimal greenThreshold, BigDecimal amberThreshold, BigDecimal redThreshold,
        String toleranceStatus, List<PerformancePoint> readings) {}
    public record ComplianceRow(String key, String displayLabel, long total, long timely, long pending, long late) {}
    public record DynamicCategoryCount(Long categoryId, String displayLabel, Long count) {}
}
