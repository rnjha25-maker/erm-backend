package ermorg.erm.dto.response;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import ermorg.erm.model.RiskAssessment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RiskAssessmentResponse {

	private long assessmentId;
	private long riskId;
	private String riskTitle;
    private Long subRiskId;
    private String subRiskName;
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
    private Double riskRatingScore;
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
    private String unitOfMeasurement;
    private String currency;
    private Long assetValue;
    private Long yearlyFrequency;
    private Double offPotentialLoss;
    private Long residualRiskRatingCriteria;
    private Double yearlyLossExpectancy;
    private LocalDate lastEvaluationDate;
    private LocalDate nextEvaluationDate;

    public RiskAssessmentResponse(RiskAssessment riskAssessment) {
    	this.assessmentId = riskAssessment.getId();
    	this.riskId = riskAssessment.getRisk().getId();
    	this.riskTitle = riskAssessment.getRisk().getRisktitle();
		if (riskAssessment.getSubRisk() != null) {
			this.subRiskId = riskAssessment.getSubRisk().getId();
			this.subRiskName = riskAssessment.getSubRisk().getSubRisk();
		}
		this.riskAnalysisType = riskAssessment.getRiskAnalysisType();
		this.likelihood = riskAssessment.getLikelihood();
		this.likelihoodProbability = riskAssessment.getLikelihoodProbability();
		this.financialImpact = riskAssessment.getFinancialImpact();
		this.operationalImpact = riskAssessment.getOperationalImpact();
		this.customerImpact = riskAssessment.getCustomerImpact();
		this.reputationalImpact = riskAssessment.getReputationalImpact();
		this.legalComplianceImpact = riskAssessment.getLegalComplianceImpact();
		this.grossImpactScore = riskAssessment.getGrossImpactScore();
		this.riskRating = riskAssessment.getRiskRating();
		this.riskRatingScore = riskAssessment.getRiskRatingScore();
		this.velocity = riskAssessment.getVelocity();
		this.riskAppetite = riskAssessment.getRiskAppetite();
		this.riskToleranceStatus = riskAssessment.getRiskToleranceStatus();
		this.riskPriority = riskAssessment.getRiskPriority();
		this.riskTreatmentStrategy = riskAssessment.getRiskTreatmentStrategy();
		this.riskAssessmentFrequency = riskAssessment.getRiskAssessmentFrequency();
		this.riskAssessmentBy = riskAssessment.getRiskAssessmentBy();
		this.riskReporting = riskAssessment.getRiskReporting();
		this.stage = riskAssessment.getStage();
		this.riskAppetiteStatus = riskAssessment.getRiskAppetiteStatus();
		this.riskAcceptanceLevel = riskAssessment.getRiskAcceptanceLevel();
		this.valueUnit = riskAssessment.getValueUnit();
		this.unitOfMeasurement = riskAssessment.getValueUnit() != null ? riskAssessment.getValueUnit().getLabel() : null;
		this.currency = riskAssessment.getCurrency();
		this.assetValue = riskAssessment.getAssetValue();
		this.yearlyFrequency = riskAssessment.getYearlyFrequency();
		this.offPotentialLoss = riskAssessment.getOffPotentialLoss();
		this.residualRiskRatingCriteria = riskAssessment.getResidualRiskRatingCriteria();
		this.yearlyLossExpectancy = riskAssessment.getYearlyLossExpectancy();
		this.lastEvaluationDate = riskAssessment.getLastEvaluationDate();
		this.nextEvaluationDate = riskAssessment.getNextEvaluationDate();
    }

    /**
     * Resolves raw IDs / numeric scores to human-readable values.
     * Call this in every service method that returns this DTO directly.
     */
    public RiskAssessmentResponse resolve(ermorg.erm.mapping.FieldMapperUtils utils) {
        this.riskRating       = utils.resolveRatingLabel(this.riskRating);
        this.riskAssessmentBy = utils.resolveUserFromObject(this.riskAssessmentBy);
        this.riskReporting    = utils.resolveUserFromObject(this.riskReporting);
        return this;
    }
}
