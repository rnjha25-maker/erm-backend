package ermorg.erm.dto.response;


import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.model.RiskReview;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskReviewResponseDtoResponse {
    
    private Long riskReviewId;
    private long riskId;
    private String riskTitle;
    private List<SubRiskResponse> subRiskResponses = new ArrayList<>();
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
    private String riskTreatmentStatus;
    private String riskToleranceStatus;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private String riskEvaluationBy;
    private long riskReporting;
    private String reviewType;
    private String status;
    private String riskEvaluationFrequency;
    private Double assetsValue;
    private Double perOfPotentialLoss;
    private Integer yearlyFrequency;
    private Double annualLossExpectancy;
    private String lastEvaluationDate;
    private String nextEvaluationDate;
    private String createdAt;
    private String updatedAt;
    
    public RiskReviewResponseDtoResponse(RiskReview riskReview) {
    	this.riskReviewId = riskReview.getId();
    	this.riskId = riskReview.getRisk().getId();
    	this.riskTitle = riskReview.getRisk().getRisktitle();
    	this.subRiskResponses = riskReview.getSubRisks() != null
				? riskReview.getSubRisks().stream().map(SubRiskResponse::new).toList()
				: new ArrayList<>();
    	this.revisedLikelihood = riskReview.getRevisedLikelihood();
		this.revisedVelocity = riskReview.getRevisedVelocity();
		this.likelihoodProbability = riskReview.getLikelihoodProbability();
		this.revisedFinancialImpact = riskReview.getRevisedFinancialImpact();
		this.revisedOperationalImpact = riskReview.getRevisedOperationalImpact();
		this.revisedCustomerImpact = riskReview.getRevisedCustomerImpact();
		this.revisedReputationalImpact = riskReview.getRevisedReputationalImpact();
		this.revisedLegalComplianceImpact = riskReview.getRevisedLegalComplianceImpact();
		this.reviseImpactScore = riskReview.getReviseImpactScore();
		this.residualRiskScoreRange = riskReview.getResidualRiskScoreRange();
		this.residualRiskRating = riskReview.getResidualRiskRating();
		this.riskTreatmentStatus = riskReview.getRiskTreatmentStatus();
		this.riskToleranceStatus = riskReview.getRiskToleranceStatus();
		this.riskAppetiteStatus = riskReview.getRiskAppetiteStatus();
		this.riskAcceptanceLevel = riskReview.getRiskAcceptanceLevel();
		this.riskEvaluationBy = riskReview.getRiskEvaluationBy();
		this.riskReporting = riskReview.getRiskReporting().getId();
		this.reviewType = riskReview.getReviewType();
		this.status = riskReview.getStatus();
		this.riskEvaluationFrequency = riskReview.getRiskEvaluationFrequency();
		this.assetsValue = riskReview.getAssetsValue();
		this.perOfPotentialLoss = riskReview.getPerOfPotentialLoss();
		this.yearlyFrequency = riskReview.getYearlyFrequency();
		this.annualLossExpectancy = riskReview.getAnnualLossExpectancy();
		this.lastEvaluationDate = riskReview.getLastEvaluationDate() != null ? riskReview.getLastEvaluationDate().toString() : null;
		this.nextEvaluationDate = riskReview.getNextEvaluationDate() != null ? riskReview.getNextEvaluationDate().toString() : null;
		this.createdAt = riskReview.getCreatedAt() != null ? riskReview.getCreatedAt().toString() : null;
		this.updatedAt = riskReview.getUpdatedAt() != null ? riskReview.getUpdatedAt().toString() : null;
    }
}
