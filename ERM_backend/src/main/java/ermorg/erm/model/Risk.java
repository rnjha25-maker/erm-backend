package ermorg.erm.model;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.RiskCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "risk", indexes = {
	@Index(name = "idx_risk_org_deleted", columnList = "organization_id, deleted"),
	@Index(name = "idx_risk_company_deleted", columnList = "company_id, deleted"),
	@Index(name = "idx_risk_org_id", columnList = "organization_id"),
	@Index(name = "idx_risk_company_id", columnList = "company_id")
})
public class Risk extends BaseModel {
	
	@Column(name="risk_title")
	private String risktitle;
	
	@Column(name="risk_source")
	private String riskSource;
	
	@Column(name="risk_category")
    private RiskCategory category;
    private String subCategory;
    private String exposure;
    
    @Column(name="business_function")
    private Long function;
	
	@Column(name="business_vertical")
    private Long businessVertical;
	
	@Column(name="business_segment")
    private String businessSegment;
	
	@ManyToOne
	@JoinColumn(name="ownerId")
    private User riskOwner;
	
	@ManyToOne
	@JoinColumn(name="champion_id")
    private User riskChampion;

	@Column(name = "organization_id")
	private Long organizationId;

	
	@Column(name = "company_id")
	private Long companyId;
	
	@Column(name="risk_creation_by_period")
    private String riskCreationByPeriod;
	
    private String riskStatus;
    private String evidanceRequired;
    private String riskRegisterType; 
    private String supportingEvidance; 
    
    
    
    @Column(name="branchId")
    private Long branchId;
	
	
	@OneToMany(mappedBy = "risk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SubRisk> subRisk = new ArrayList<>();

	@OneToOne(mappedBy = "risk", fetch = FetchType.LAZY)
	private RiskAssessment riskAssessment;

}
