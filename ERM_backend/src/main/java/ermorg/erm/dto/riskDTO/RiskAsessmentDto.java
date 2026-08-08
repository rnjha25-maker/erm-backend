package ermorg.erm.dto.riskDTO;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RiskAsessmentDto {

	private long assessmentId;
	private long riskId;
    private Long subRiskId;
    private String riskAnalysisType;
    private String likelihood;
    private String likelihoodProbability;
    private String financialImpact;
    private String operationalImpact;
    private String customerImpact;
    private String reputationalImpact;
    private String legalComplianceImpact;
    private String grossImpactScore;
    private String riskRating;
    private String velocity;
    private String riskAppetite;
    private String riskToleranceStatus;
    private String riskPriority;
    private String riskTreatmentStrategy;
    private String riskAssessmentFrequency;
    private String riskAssessmentBy;
    private String riskReporting;
    private String stage;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private RiskValueUnit valueUnit;
    private String currency;
    private Long assetValue;
    private Double offPotentialLoss;
    private Long yearlyFrequency;
    private Double yearlyLossExpectancy;
    private Double riskRatingScore;
    private Long residualRiskRatingCriteria;
    private LocalDate lastEvaluationDate;
    private LocalDate nextEvaluationDate;
}
