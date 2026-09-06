package ermorg.erm.dto.response;

import java.math.BigDecimal;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpaKpiReviewResponseDTO {

    private long kpaKpiReviewId;
    private String kpa;
    private String riskTitle;
    private String riskSubTitle;
    private String businessObjectives;
    private String businessFunction;
    private String departmentFunction;
    private String departmentName;
    private String department;
    private long ownerId;
    private String ownerName;
    private Long riskOwner;
    private String riskOwnerName;
    private Long businessFunctionalOwner;
    private String functionalOwner;
    private String evaluationByName;
    private String target;
    private String keyPerformanceArea;
    private String keyPerformanceParameters;
    private String keyPerformanceIndicator;
    private String keyRiskIndicator;
    private String keyRiskIndicatorKri;
    private String keyPerformanceIndicators;
    private String typesOfKpi;
    private String typesOfKeyRiskIndicator;
    private String typesOfKeyRiskIndicatorKri;
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
    private Long reportingId;
    private String reportingName;
    private String reporting;
    private String reportingFrequency;
    private String unitOfMeasurement;
    private String currency;
    private RiskValueUnit valueUnit;
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
    private String riskAppetiteLevel;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private long kpiEvaluationBy;
    private long kriEvaluationBy;
    private String kriEvaluationByName;
    private String evaluationBy;
    private String evaluationByNo;
    private String kpiEvaluationFrequency;
    private String kriEvaluationFrequency;
    private String keyRiskEvaluationFrequency;
    private BigDecimal potentialLossPercentage;
    private Integer yearlyFrequency;
    private BigDecimal annualLossExpectancy;
    private String dueDate;
    private String actualDate;
    private String lastKpiEvaluationDate;
    private String nextEvaluationDate;
    private String status;
}
