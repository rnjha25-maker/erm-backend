package ermorg.erm.model;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "risk_reviews")
@Data
@EqualsAndHashCode(callSuper = false)
public class RiskReview extends BaseModel {

	@ManyToOne
	@JoinColumn(name = "risk_id")
	private Risk risk;

	
	@OneToMany(mappedBy = "riskReview")
	private List<SubRisk> subRisks = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;
	
	@Column(name = "revised_likelihood")
	private String revisedLikelihood;

	@Column(name = "revised_velocity")
	private String revisedVelocity;

	@Column(name = "likelihood_probability")
	private String likelihoodProbability;

	@Column(name = "revised_financial_impact")
	private String revisedFinancialImpact;

	@Column(name = "revised_operational_impact")
	private String revisedOperationalImpact;

	@Column(name = "revised_customer_impact")
	private String revisedCustomerImpact;

	@Column(name = "revised_reputational_impact")
	private String revisedReputationalImpact;

	@Column(name = "revised_legal_compliance_impact")
	private String revisedLegalComplianceImpact;

	@Column(name = "revise_impact_score")
	private String reviseImpactScore;

	@Column(name = "residual_risk_score_range")
	private String residualRiskScoreRange;

	@Column(name = "residual_risk_rating")
	private String residualRiskRating;

	@Column(name = "risk_treatment_status")
	private String riskTreatmentStatus;

	@Column(name = "risk_tolerance_status")
	private String riskToleranceStatus;

	@Column(name = "risk_appetite_status")
	private String riskAppetiteStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_acceptance_level", length = 100)
	private RiskAcceptanceLevel riskAcceptanceLevel;

	@Enumerated(EnumType.STRING)
	@Column(name = "value_unit", length = 30)
	private RiskValueUnit valueUnit;

	@Column(name = "currency", length = 10)
	private String currency;

	@Column(name = "risk_evaluation_by")
	private String riskEvaluationBy;

	@ManyToOne
	@JoinColumn(name = "risk_reporting")
	private User riskReporting;

	@Column(name = "status")
	private String status;

	@Column(name = "risk_evaluation_frequency")
	private String riskEvaluationFrequency;

	@Column(name = "assets_value")
	private Double assetsValue;

	@Column(name = "per_of_potential_loss")
	private Double perOfPotentialLoss;

	@Column(name = "yearly_frequency")
	private Integer yearlyFrequency;

	@Column(name = "annual_loss_expectancy")
	private Double annualLossExpectancy;

	@Column(name = "last_evaluation_date")
	private LocalDate lastEvaluationDate;

	@Column(name = "next_evaluation_date")
	private LocalDate nextEvaluationDate;
	
	@Column(name = "review_type")
	private String reviewType;

}
