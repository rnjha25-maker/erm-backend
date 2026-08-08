package ermorg.erm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import ermorg.erm.constant.RiskAcceptanceLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
@Table(name = "erm_maturity_assessments", indexes = {
	@jakarta.persistence.Index(name = "idx_ermaturity_org_deleted", columnList  = "organization_id, deleted"),
	@jakarta.persistence.Index(name = "idx_ermaturity_org_erm_id_deleted", columnList = "organization_id, erm_maturity_id, deleted")
})
@Entity
public class ERMMaturityAssessment extends BaseModel {

	@Column(name = "status")
	private String status;

	@Column(name = "overall_maturity_level")
	private String overallMaturityLevel;

	@Column(name = "assessed_by")
	private String assessedBy;

	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "actual_date")
	private Date actualDate;

	@Column(name = "last_assessment_date")
	private Date lastAssessmentDate;

	@Column(name = "next_assessment_date")
	private Date nextAssessmentDate;

	@Column(name = "erm_maturity_id")
	private String ermMaturityId;

	@ElementCollection
	@CollectionTable(name = "erm_maturity_department_ids", joinColumns = @JoinColumn(name = "maturity_assessment_id"))
	@Column(name = "department_id")
	private List<Long> departmentIds = new ArrayList<>();

	@Column(name = "risk_appetite_status")
	private String riskAppetiteStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_acceptance_level", length = 100)
	private RiskAcceptanceLevel riskAcceptanceLevel;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

	@BatchSize(size = 50)
	@OneToMany(mappedBy = "maturityAssessment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ERMMaturityScore> scores = new ArrayList<>();
}
