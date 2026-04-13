package com.erm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kri_kpi_reviews")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KriKpiReview extends BaseModel {

	
	@ManyToOne
	@JoinColumn(name="risk_id")
	private Risk risk;
	
	@OneToMany(mappedBy="kriKpiReview")
	private List<SubRisk> subRisks = new ArrayList<>();
	
	@Column(name = "business_objectives")
	private String businessObjectives;

	@Column(name = "business_function")
	private String businessFunction;

	@ManyToOne
	@JoinColumn(name = "risk_owner_id")
	private User riskOwner;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(name = "target")
	private String target;

	@Column(name = "key_risk_parameters")
	private String keyRiskParameters;

	@Column(name = "key_risk_indicator_kri")
	private String keyRiskIndicatorKri;

	@Column(name = "types_of_key_risk_indicator_kri")
	private String typesOfKeyRiskIndicatorKri;

	@Column(name = "performance_indicators")
	private String performanceIndicators;

	@Column(name = "stakeholder_departments")
	private String stakeholderDepartments;

	@Column(name = "risk_tolerance_range_min_value")
	private String riskToleranceRangeMinValue;

	@Column(name = "risk_tolerance_range_max_value")
	private String riskToleranceRangeMaxValue;

	@Column(name = "targets")
	private String targets;

	@Column(name = "activities")
	private String activities;

	@Column(name = "thresholds")
	private String thresholds;

	@Column(name = "risk_appetite")
	private String riskAppetite;

	@Column(name = "escalation_matrix")
	private String escalationMatrix;

	@Column(name = "level_of_measurement_level")
	private String levelOfMeasurementLevel;

	@Column(name = "reporting_frequency")
	private String reportingFrequency;

	@Column(name = "currency")
	private String currency;

	private String targetValue;

	private String actualValue;

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
	@Column(name = "kri_appetite_status")
	private String kriAppetiteStatus;

	@ManyToOne
	@JoinColumn(name = "kri_evaluation_by")
	private User kriEvaluationBy;

	@Column(name = "kri_evaluation_frequency")
	private String kriEvaluationFrequency;

	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "actual_date")
	private Date actualDate;

//	@Column(name = "dashboard_kri_status")
//	private String dashboardKriStatus;

	@Column(name = "last_kri_evaluation_date")
	private Date lastKriEvaluationDate;

	@Column(name = "next_evaluation_date")
	private Date nextEvaluationDate;

	@Column(name = "status")
	private String status;

}
