package com.erm.dto.riskDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KriKpiReviewRequestDTO {
	
	private long kriId;
    private String businessObjectives;
    private String businessFunction;
    private long riskId;//new
    private long riskOwner;
    private List<Long> subRiskIds = new ArrayList<>();//new
    private String target;
    private String keyRiskParameters;
    private String keyRiskIndicatorKri;
    private String typesOfKeyRiskIndicatorKri;
    private String performanceIndicators;
    private String typeOfRiskIndicator;//new

    private String stakeholderDepartments;
    
    private String riskToleranceRangeMinValue;
    
    private String riskToleranceRangeMaxValue;
    
    private String targets;
    private String riskToleranceStatus; //new
    private String activities;
    private String thresholds;
    private String riskAppetite;
    private String escalationMatrix;
    private String measurableParameters;
    private long reporting; //new
    private String unitOfMeasurement; //new
    private String reportingFrequency;
    private String currency;
    private String targetValue;
    private String actualValue; // new
    private String actuals;
    private String january;
    private String february;
    private String march;
    private String april;
    private String may;
    private String june;
    private String july;
    private String august;
    private String september;
    private String october;
    private String november;
    private String december;
    private String q1;
    private String q2;
	private String q3;
	private String q4;
	private String kriType;
    private String kriAppetiteStatus;
    private long kriEvaluationBy;
    private String kriEvaluationFrequency;
    private Date dueDate;
    private Date actualDate;
    private String dashboardKriStatus;
    private String lastKriEvaluationDate;
    private Date nextEvaluationDate;
    private String status;

}
