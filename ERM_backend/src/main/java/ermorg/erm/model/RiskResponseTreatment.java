package ermorg.erm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ermorg.erm.constant.RiskAcceptanceLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "risk_risponse_treatment")
public class RiskResponseTreatment extends BaseModel {

	@ManyToOne
	@JoinColumn(name = "risk_id")
	private Risk risk;

	
	@OneToMany(mappedBy = "riskResponseTreatment")
	private List<SubRisk> subRisks = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(name = "control_presence")
	private String controlPresence;

	@Column(name = "control_description")
	private String controlDescription;

	@Column(name = "control_gaps_identified")
	private String controlGapsIdentified;

	@Column(name = "recommended_control")
	private String recommendedControl;

	@Column(name = "management_action_plan")
	private String managementActionPlan;

	@Column(name = "contingency_plans")
	private String contingencyPlans;

	@Column(name = "control_effectiveness_percentage")
	private String controlEffectivenessPercentage;

	@Column(name = "control_effectiveness_weightage")
	private String controlEffectivenessWeightage;

	@Column(name = "control_evaluation_status")
	private String controlEvaluationStatus;

	@Column(name = "risk_treatment_status")
	private String riskTreatmentStatus;

	@Column(name = "risk_appetite_status")
	private String riskAppetiteStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_acceptance_level", length = 100)
	private RiskAcceptanceLevel riskAcceptanceLevel;

	@Column(name = "evidence_require")
	private String evidenceRequire;

	@Column(name = "supporting_evidence_artifacts_description")
	private String supportingEvidence;

	@Column(name = "supporting_evidence_document", length = 36, columnDefinition = "CHAR(36)")
	private UUID supportingEvidenceDocument;

	@Column(name = "control_evaluation_by")
	private String controlEvaluationBy;

	@ManyToOne
	@JoinColumn(name = "risk_reporting_id")
	private User riskReporting;

	@Column(name = "control_status")
	private String controlStatus;
}
