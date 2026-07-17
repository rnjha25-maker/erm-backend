package ermorg.erm.dto.response;

import java.math.BigDecimal;

import ermorg.erm.constant.RiskAcceptanceLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpaKpiReviewResponseDTO {

    private long kpaKpiReviewId;
    private String kpa;
    private String businessObjectives;
    private String businessFunction;
    private String departmentFunction;
    private long ownerId;
    private Long businessFunctionalOwner;
    private String target;
    private String keyPerformanceArea;
    private String keyPerformanceParameters;
    private String keyPerformanceIndicator;
    private String keyPerformanceIndicators;
    private String typesOfKpi;
    private String performanceIndicators;
    private String stakeholderDepartments;
    private BigDecimal performanceToleranceMinValue;
    private BigDecimal performanceToleranceMaxValue;
    @Deprecated
    private BigDecimal riskToleranceRangeMinValue;
    @Deprecated
    private BigDecimal riskToleranceRangeMaxValue;
    private String targets;
    private String activities;
    private String thresholds;
    private String performanceAppetite;
    @Deprecated
    private String riskAppetite;
    private String escalationMatrix;
    private String measurableParameters;
    private String reporting;
    private String reportingFrequency;
    private String unitOfMeasurement;
    private String currency;
    private BigDecimal targetValue;
    private BigDecimal actualValue;
    private BigDecimal actuals;
    private BigDecimal january;
    private BigDecimal february;
    private BigDecimal march;
    private BigDecimal april;
    private BigDecimal may;
    private BigDecimal june;
    private BigDecimal july;
    private BigDecimal august;
    private BigDecimal september;
    private BigDecimal october;
    private BigDecimal november;
    private BigDecimal december;
    private BigDecimal q1;
    private BigDecimal q2;
    private BigDecimal q3;
    private BigDecimal q4;
    private String monthlyValues;
    private String quarterlyValues;
    private String kpiType;
    private String kraRating;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private long kpiEvaluationBy;
    private Long evaluationBy;
    private String kpiEvaluationFrequency;
    private BigDecimal potentialLossPercentage;
    private Integer yearlyFrequency;
    private BigDecimal annualLossExpectancy;
    private String dueDate;
    private String actualDate;
    private String lastKpiEvaluationDate;
    private String nextEvaluationDate;
    private String status;
}
