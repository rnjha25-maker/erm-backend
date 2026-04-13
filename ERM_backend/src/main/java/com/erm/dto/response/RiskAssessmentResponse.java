package com.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.erm.model.RiskAssessment;

import lombok.Data;

@Data
public class RiskAssessmentResponse {

	private long assessmentId;
	private long riskId;
    private List<Long> subRiskIds = new ArrayList<>();
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
		this.subRiskIds = riskAssessment.getSubRisks().stream().map(sbRisk -> sbRisk.getId()).collect(Collectors.toList());
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
