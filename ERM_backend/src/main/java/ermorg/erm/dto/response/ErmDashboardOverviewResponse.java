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
}
