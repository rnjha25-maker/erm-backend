package ermorg.erm.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "risk_treatment")
public class RiskTreatment extends BaseModel{
	
	@ManyToOne
	@JoinColumn(name = "risk_id")
    private Risk risk;
    
    @ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;
    private String riskTitle;
    private String riskSubTitle;
    private String controlId;
    private String controlSource;
    private String controlTitle;
    private String subControlTitle;
    private Integer numberOfControlRisk;
    private String process;
    private String subProcess;
    private String controlPresence;
    private String importance;
    private String controlPurpose;
    private String controlMechanism;
    private String accessControls;
    private String processControls;
    private String physicalControls;
    private String technicalControl;
    private String existingControls;
    private String controlGapsIdentified;
    private String recommendedControl;
    private String contingencyPlans;
    private String managementActionPlan;
    private String controlEvaluationStatus;
    private Integer controlEffectivenessPercent;
    private String residualRiskRating;
    private String evidenceRequires;
    private String supportingEvidenceArtifactsDescription;
    private String controlAssessmentFrequency;
    private Date dueDate;
    private Date actualDate;
    private Integer overdueInDays;
    private String primaryResponsible;
    private String approver;
    private String controlEvaluationBy;
    private String riskReporting;
    private String controlByPeriod;
    private Date dateOfControlCreation;
    private String controlCreatedBy;
    private String controlApprovedBy;
    private Date controlAssessmentDate;
    private Date lastControlAssessmentDate;
    private Date nextControlAssessmentDate;
    private String status;


}
