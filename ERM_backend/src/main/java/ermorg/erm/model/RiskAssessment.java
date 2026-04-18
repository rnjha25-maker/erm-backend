package ermorg.erm.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="risk_assessment", indexes = {
	@jakarta.persistence.Index(name = "idx_riskassessment_org_deleted", columnList  = "organization_id,deleted")
})
@Entity
public class RiskAssessment  extends BaseModel{

	@OneToOne
	@JoinColumn(name="risk_id")
    private Risk risk;
    
	@OneToMany(mappedBy="riskAssessment")
	private List<SubRisk> subRisks = new ArrayList<>();
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;
	
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
    
  //newly added
	
  	@Column(name="asset_value")
  	private long assetValue;
  	@Column(name="off_potential_loss")
  	private double offPotentialLoss;
  	@Column(name="yearly_frequency")
  	private long yearlyFrequency;
  	@Column(name="yearly_loss_expectancy")
  	private double yearlyLossExpectancy;
  	@Column(name="risk_rating_score")
  	private double riskRatingScore;
  	@Column(name="residual_risk_rating_criteria")
  	private long residualRiskRatingCriteria;
}
