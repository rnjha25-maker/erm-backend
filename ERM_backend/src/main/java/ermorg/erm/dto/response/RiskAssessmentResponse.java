package ermorg.erm.dto.response;

import ermorg.erm.model.RiskAssessment;

import lombok.Data;

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
    private String velocity;
    private String riskAppetite;
    private String riskToleranceStatus;
    private String riskPriority;
    private String riskTreatmentStrategy;
    private String riskAssessmentFrequency;
    private String riskAssessmentBy;
    private String riskReporting;
    
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
		this.velocity = riskAssessment.getVelocity();
		this.riskAppetite = riskAssessment.getRiskAppetite();
		this.riskToleranceStatus = riskAssessment.getRiskToleranceStatus();
		this.riskPriority = riskAssessment.getRiskPriority();
		this.riskTreatmentStrategy = riskAssessment.getRiskTreatmentStrategy();
		this.riskAssessmentFrequency = riskAssessment.getRiskAssessmentFrequency();
		this.riskAssessmentBy = riskAssessment.getRiskAssessmentBy();
		this.riskReporting = riskAssessment.getRiskReporting();
		
    }
	
}
