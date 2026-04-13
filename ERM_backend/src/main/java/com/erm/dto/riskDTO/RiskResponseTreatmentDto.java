package com.erm.dto.riskDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskResponseTreatmentDto {

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
    
    
}
