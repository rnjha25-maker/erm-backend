package com.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.erm.model.RiskResponseTreatment;

import lombok.Data;

@Data
public class RiskResponseTreatmentResponse {

	private long riskResponseTreatmentId;
	private long riskId;
    private List<Long> riskSubIds = new ArrayList<>();
    private String controlPresence;
    private String controlDescription;
    private String controlGapsIdentified;
    private String recommendedControl;
    private String managementActionPlan;
    private String contingencyPlans;
    private String controlEffectiveness;
    private String controlEffectivenessWeightage;
    private String controlEvaluationStatus;
    private String riskTreatmentStatus;
    private String evidenceRequire;
    private String supportingEvidence;
    private String controlEvaluationBy;
    private long riskReporting;
    private String controlStatus;
    
    public RiskResponseTreatmentResponse(RiskResponseTreatment riskResponseTreatment) {
	    this.riskResponseTreatmentId = riskResponseTreatment.getId();
    	this.riskId = riskResponseTreatment.getRisk().getId();
    	this.riskSubIds = riskResponseTreatment.getSubRisks().stream().map(s -> s.getId()).toList();
    	this.controlPresence = riskResponseTreatment.getControlPresence();
		this.controlDescription = riskResponseTreatment.getControlDescription();
		this.controlGapsIdentified = riskResponseTreatment.getControlGapsIdentified();
		this.recommendedControl = riskResponseTreatment.getRecommendedControl();
		this.managementActionPlan = riskResponseTreatment.getManagementActionPlan();
		this.contingencyPlans = riskResponseTreatment.getContingencyPlans();
		this.controlEffectiveness = riskResponseTreatment.getControlEffectivenessPercentage();
		this.controlEffectivenessWeightage = riskResponseTreatment.getControlEffectivenessWeightage();
		this.controlEvaluationStatus = riskResponseTreatment.getControlEvaluationStatus();
		this.riskTreatmentStatus = riskResponseTreatment.getRiskTreatmentStatus();
		this.evidenceRequire = riskResponseTreatment.getEvidenceRequire();
		this.supportingEvidence = riskResponseTreatment.getSupportingEvidence();
		this.controlEvaluationBy = riskResponseTreatment.getControlEvaluationBy();
		this.riskReporting = riskResponseTreatment.getRiskReporting().getId();
		this.controlStatus = riskResponseTreatment.getControlStatus();
    }
    
}
