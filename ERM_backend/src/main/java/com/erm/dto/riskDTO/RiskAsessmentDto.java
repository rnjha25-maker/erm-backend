package com.erm.dto.riskDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskAsessmentDto {
	
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
    
  //new fields
    private long assetValue;
    private double offPotentialLoss;
    private long yearlyFrequency;
    private double yearlyLossExpectancy;
    private double riskRatingScore;
    private long residualRiskRatingCriteria;
//    private String riskRating;

}
