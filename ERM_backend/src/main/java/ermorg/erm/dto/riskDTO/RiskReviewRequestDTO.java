package ermorg.erm.dto.riskDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskReviewRequestDTO {
    
	private Long riskReviewId;
    private long riskId;
    private List<Long> subRiskIds = new ArrayList<>();
    private String revisedLikelihood;
    private String revisedVelocity;
    private String likelihoodProbability;
    private String revisedFinancialImpact;
    private String revisedOperationalImpact;
    private String revisedCustomerImpact;
    private String revisedReputationalImpact;
    private String revisedLegalComplianceImpact;
    private String reviseImpactScore;
    private String residualRiskScoreRange;
    private String residualRiskRating;
    private String residualRiskRatingCriteria;
    private String riskTreatmentStatus;
    private String riskToleranceStatus;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private RiskValueUnit valueUnit;
    private String currency;
    private String riskEvaluationBy;
    private long riskReporting;
    private String reviewType;
    private String status;
    private String riskEvaluationFrequency;
    private Double assetsValue;
    private Double perOfPotentialLoss;
    private Integer yearlyFrequency;
    private Double annualLossExpectancy;
    private LocalDate lastEvaluationDate;
    private LocalDate nextEvaluationDate;
}
