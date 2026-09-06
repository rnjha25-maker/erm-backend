package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.model.RiskResponseTreatment;

import lombok.Data;

@Data
public class RiskResponseTreatmentResponse {

	private long riskResponseTreatmentId;
	private long riskId;
	private String riskTitle;
    private List<SubRiskResponse> subRisk = new ArrayList<>();
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
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;
    private String evidenceRequire;
    private String supportingEvidence;
    private UUID supportingEvidenceDocument;
    private String controlEvaluationBy;
    private long riskReporting;
    private String riskReportingName;
    private String controlStatus;
    
    public RiskResponseTreatmentResponse(RiskResponseTreatment riskResponseTreatment) {
	    this.riskResponseTreatmentId = riskResponseTreatment.getId();
    	this.riskId = riskResponseTreatment.getRisk().getId();
    	this.riskTitle = riskResponseTreatment.getRisk().getRisktitle();
    	this.subRisk = riskResponseTreatment.getSubRisks() != null
				? riskResponseTreatment.getSubRisks().stream().map(SubRiskResponse::new).toList()
				: Collections.emptyList();
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
		this.riskAppetiteStatus = riskResponseTreatment.getRiskAppetiteStatus();
		this.riskAcceptanceLevel = riskResponseTreatment.getRiskAcceptanceLevel();
		this.evidenceRequire = riskResponseTreatment.getEvidenceRequire();
		this.supportingEvidence = riskResponseTreatment.getSupportingEvidence();
		this.supportingEvidenceDocument = riskResponseTreatment.getSupportingEvidenceDocument();
		this.controlEvaluationBy = riskResponseTreatment.getControlEvaluationBy();
		this.riskReporting = riskResponseTreatment.getRiskReporting().getId();
		this.controlStatus = riskResponseTreatment.getControlStatus();
    }

    /**
     * Resolves raw IDs to human-readable values.
     * Call this in every service method that returns this DTO directly.
     */
    public RiskResponseTreatmentResponse resolve(ermorg.erm.mapping.FieldMapperUtils utils) {
        this.controlEvaluationBy = utils.resolveUserFromObject(this.controlEvaluationBy);
        this.riskReportingName   = utils.resolveUser(this.riskReporting);
        return this;
    }
}
