package ermorg.erm.dto.response;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<Long> subRiskIds = new ArrayList<>();
    private String revisedLikelihood;
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
    private String riskEvaluationBy;
    private long riskReporting;
    private String reviewType;
    private String status;
    private String riskEvaluationFrequency;
    private String lastEvaluationDate;
    private String nextEvaluationDate;
    private String createdAt;
    private String updatedAt;
    
    public RiskReviewResponseDtoResponse(RiskReview riskReview) {
    	this.riskReviewId = riskReview.getId();
    	this.riskId = riskReview.getRisk().getId();
    	this.subRiskIds = riskReview.getSubRisks().stream().map(r->r.getId()).collect(Collectors.toList());
    	this.revisedLikelihood = riskReview.getRevisedLikelihood();
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
		this.riskEvaluationBy = riskReview.getRiskEvaluationBy();
		this.riskReporting = riskReview.getRiskReporting().getId();
		this.reviewType = riskReview.getReviewType();
		this.status = riskReview.getStatus();
		this.riskEvaluationFrequency = riskReview.getRiskEvaluationFrequency();
		this.lastEvaluationDate = riskReview.getLastEvaluationDate().toString();
		this.nextEvaluationDate = riskReview.getNextEvaluationDate().toString();
		this.createdAt = riskReview.getCreatedAt().toString();
		this.updatedAt = riskReview.getUpdatedAt().toString();
    }
}
