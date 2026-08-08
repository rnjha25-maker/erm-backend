package ermorg.erm.dto.riskDTO;

import java.math.BigDecimal;
import java.util.Date;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpaKpiReviewRequestDTO {

    private long kpaKpiReviewId;
    @NotBlank
    @Size(max = 255)
    private String kpa;
    @Size(max = 1000)
    private String businessObjectives;
    @Size(max = 255)
    private String businessFunction;
    @Positive
    private long ownerId;
    @Size(max = 1000)
    private String target;
    @Size(max = 1000)
    private String keyPerformanceParameters;
    @NotBlank
    @Size(max = 1000)
    private String keyPerformanceIndicator;
    @Size(max = 100)
    private String typesOfKpi;
    @Size(max = 1000)
    private String performanceIndicators;
    @Size(max = 1000)
    private String stakeholderDepartments;
    @DecimalMin("0.0000")
    private BigDecimal performanceToleranceMinValue;
    @DecimalMin("0.0000")
    private BigDecimal performanceToleranceMaxValue;
    @Deprecated
    private BigDecimal riskToleranceRangeMinValue;
    @Deprecated
    private BigDecimal riskToleranceRangeMaxValue;
    @Size(max = 1000)
    private String targets;
    @Size(max = 1000)
    private String activities;
    @Size(max = 1000)
    private String thresholds;
    @Size(max = 1000)
    private String performanceAppetite;
    @Deprecated
    private String riskAppetite;
    @Size(max = 1000)
    private String escalationMatrix;
    @Size(max = 255)
    private String measurableParameters;
    @NotBlank
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|HALF_YEARLY|ANNUALLY|AD_HOC",
            message = "reportingFrequency must be DAILY, WEEKLY, MONTHLY, QUARTERLY, HALF_YEARLY, ANNUALLY, or AD_HOC")
    private String reportingFrequency;
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code")
    private String currency;
    private RiskValueUnit valueUnit;
    @DecimalMin("0.0000")
    private BigDecimal targetValue;
    @DecimalMin("0.0000")
    private BigDecimal actualValue;
    @DecimalMin("0.0000")
    private BigDecimal january;
    @DecimalMin("0.0000")
    private BigDecimal february;
    @DecimalMin("0.0000")
    private BigDecimal march;
    @DecimalMin("0.0000")
    private BigDecimal april;
    @DecimalMin("0.0000")
    private BigDecimal may;
    @DecimalMin("0.0000")
    private BigDecimal june;
    @DecimalMin("0.0000")
    private BigDecimal july;
    @DecimalMin("0.0000")
    private BigDecimal august;
    @DecimalMin("0.0000")
    private BigDecimal september;
    @DecimalMin("0.0000")
    private BigDecimal october;
    @DecimalMin("0.0000")
    private BigDecimal november;
    @DecimalMin("0.0000")
    private BigDecimal december;
    @DecimalMin("0.0000")
    private BigDecimal q1;
    @DecimalMin("0.0000")
    private BigDecimal q2;
    @DecimalMin("0.0000")
    private BigDecimal q3;
    @DecimalMin("0.0000")
    private BigDecimal q4;
    @Size(max = 50)
    private String kpiType;
    @Size(max = 30)
    private String kraRating;
    @Size(max = 255)
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    @Positive
    private long kpiEvaluationBy;
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|HALF_YEARLY|ANNUALLY|AD_HOC",
            message = "kpiEvaluationFrequency must be DAILY, WEEKLY, MONTHLY, QUARTERLY, HALF_YEARLY, ANNUALLY, or AD_HOC")
    private String kpiEvaluationFrequency;
    @DecimalMin("0.0000")
    @DecimalMax("100.0000")
    private BigDecimal potentialLossPercentage;
    @Min(0)
    private Integer yearlyFrequency;
    @FutureOrPresent
    private Date dueDate;
    @PastOrPresent
    private Date actualDate;
    @PastOrPresent
    private Date lastKpiEvaluationDate;
    @FutureOrPresent
    private Date nextEvaluationDate;
    @NotBlank
    @Pattern(regexp = "DRAFT|ACTIVE|IN_REVIEW|COMPLETED|OVERDUE|INACTIVE",
            message = "status must be DRAFT, ACTIVE, IN_REVIEW, COMPLETED, OVERDUE, or INACTIVE")
    private String status;
}
