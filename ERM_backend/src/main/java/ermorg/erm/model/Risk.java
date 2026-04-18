package ermorg.erm.model;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.BusinessVertical;
import ermorg.erm.constant.Functions;
import ermorg.erm.constant.RiskCategory;
import ermorg.erm.constant.RiskSubCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@org.hibernate.annotations.Table(appliesTo = "risk", 
	indexes = {
		@org.hibernate.annotations.Index(name = "idx_risk_org_deleted", columnNames = {"organizationId", "deleted"}),
		@org.hibernate.annotations.Index(name = "idx_risk_company_deleted", columnNames = {"companyId", "deleted"}),
		@org.hibernate.annotations.Index(name = "idx_risk_org_id", columnNames = {"organizationId"}),
		@org.hibernate.annotations.Index(name = "idx_risk_company_id", columnNames = {"companyId"})
	}
)
public class Risk extends BaseModel {
	
	@Column(name="risk_title")
	private String risktitle;
	
	@Column(name="risk_source")
	private String riskSource;
	
	@Column(name="risk_category")
    private RiskCategory category;
    private RiskSubCategory subCategory;
    private String exposure;
    
    @Column(name="business_function")
    private Functions function;
	
	@Column(name="business_vertical")
    private BusinessVertical businessVertical;
	
	@Column(name="business_segment")
    private String businessSegment;
	
	@ManyToOne
	@JoinColumn(name="ownerId")
    private User riskOwner;
	
	@ManyToOne
	@JoinColumn(name="champion_id")
    private User riskChampion;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "organization_id")
//	private Organization organization;
	
	@Column(name = "organization_id")
	private Long organizationId;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "company_id")
//	private Company company;
	
	@Column(name = "company_id")
	private Long companyId;
	
	@Column(name="risk_creation_by_period")
    private String riskCreationByPeriod;
	
    private String riskStatus;
    private String evidanceRequired;
    private String riskRegisterType; 
    private String supportingEvidance; 
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="branchId")
//    private Branch branch;
    
    
    @Column(name="branchId")
    private Long branchId;
	
	
	@OneToMany(mappedBy = "risk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SubRisk> subRisk = new ArrayList<>();
	
	//newly added
	
//	@Column(name="asset_value")
//	private long assetValue;
//	@Column(name="off_potential_loss")
//	private double offPotentialLoss;
//	@Column(name="yearly_frequency")
//	private long yearlyFrequency;
//	@Column(name="yearly_loss_expectancy")
//	private double yearlyLossExpectancy;
//	@Column(name="risk_rating_score")
//	private double riskRatingScore;
//	@Column(name="residual_risk_rating_criteria")
//	private long residualRiskRatingCriteria;
//	@Column(name="risk_rating")
//	private String riskRating;
//	private Integer overdueInDays;
//	private int financialYear;
//	@Enumerated(EnumType.STRING)
//	private Period period;
//	private String overallOverview;
//	@Column(name = "risk_title")
//	private String risk;
//	@Enumerated(EnumType.STRING)
//	@Column(name = "risk_category")
//	private RiskCategory category;
//	@Enumerated(EnumType.STRING)
//	private Likelihood likelihood;
//	@Enumerated(EnumType.STRING)
//	private Impact impact;
//	@Enumerated(EnumType.STRING)
//	private Velocity velocity;
//	@Enumerated(EnumType.STRING)
//	@Column(name = "risk_priority")
//	private Priority riskPriority;
//	private String country;
//	private String state;
//	private String district;
//	private String department;
//	private String location;
//	private String riskLevel;
//	@Column(name = "business_segment")
//	private String businessSegmentLevelRisk;
//	private String divisionLevelRisk;
//	private String process;
//	private String mitigationStrategy;
//	private String appetite;
//	private String tolerance;
//	private String annualLossExpectancy;
	

	
//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "translation_id")
//	private RiskTranslation riskTranslation;

//	private String riskRegisterType;
//	private String businessVertical;
//	@Column(name = "business_function")
//	private String function;
//	private String businessObjectives;
//	private String riskAnalysysType;
//	private String likelihoodScore;
//	private String customerImpactValue;
//	private String customerImpactSeverity;
//	private String customerImpactScore;
//	private String financialImpactScore;
//	private String operationalImpactValue;
//	private String operationalImpactServerity;
//	private String operationalImpactScore;
//	private String legalAndComplianceImpact;
//	private String legalAndComplianceImpactSeverity;
//	private String legalAndComplianceImpactScore;
//	private String reputationalImpact;
//	private String reputationalImpactSeverity;
//	private String reputationalImpactScore;
//	private String grossImpactValue;
//	private String valueAtRisk;
//	private String grossImpactScore;
//	private String inherentRiskScore;
//	private String inherentRiskRating;
//	private String vilocity;
//	private String riskAppetite;
//	private String riskTreatmentStrategy;
//	private String riskTreatmentStatus;
//	private String controllEffectiveness;
//	private String controllEvaluationStatus;
//	private String residualRiskScore;
//	private String residualRiskValue;
//	private String revisedLikelihood;
//	private String revisedImpact;
//	private String residualRiskRating;
//	private String riskAssessmentFrequency;
//	private Date dueDate;
//	private Date acualDate;
//	private Integer overdueInDays;
//	private String riskOwner;
//	private String riskOwnerName;
//	private String primaryResponsible;
//	private String secondaryResponsible;
//	private String approver;
//	private String riskEvaluationBy;
//	private String riskReporting;
//	private String riskCreationByPeriod;
//	private String riskAssessmentByPeriod;
//	private String alerts;
//	private String emailIdsRiskOwner;
//	private String emailIdsPrimaryResponsible;
//	private String emailIdsSecondaryResponsible;
//	private String evidenceRequires;
//	private String supportingEvidenceArtifactsDescription;
//	private String riskCreatedBy;
//	private Date lastRiskAssessmentDate;
//	private Date nextRiskAssessmentDate;
//	private String status;
//	private String withinRiskAppetite;
//	private String riskAppetiteBreached;
//	private String riskToleranceBreached;

}