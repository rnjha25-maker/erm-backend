package ermorg.erm.model.history;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.constant.BusinessVertical;
import ermorg.erm.constant.Functions;
import ermorg.erm.constant.Impact;
import ermorg.erm.constant.Likelihood;
import ermorg.erm.constant.Period;
import ermorg.erm.constant.Priority;
import ermorg.erm.constant.RiskCategory;
import ermorg.erm.constant.RiskSubCategory;
import ermorg.erm.constant.Velocity;
import ermorg.erm.model.BaseModel;
import ermorg.erm.model.Branch;
import ermorg.erm.model.Risk;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RiskHistory extends BaseModel {

	private Long riskId;
	
	@Column(name="risk_title")
	private String risktitle;
	
	@Column(name="risk_source")
	private String riskSource;
	
	@Column(name="risk_category")
    private RiskCategory category;
    private String subCategory;
    private String exposure;
    
    @Column(name="business_function")
    private int function;
	
	@Column(name="business_vertical")
    private int businessVertical;
	
	@Column(name="business_segment")
    private String businessSegment;
	
	@ManyToOne
	@JoinColumn(name="ownerId")
    private User riskOwner;
	
	@ManyToOne
	@JoinColumn(name="champion_id")
    private User riskChampion;
	
	@Column(name="risk_creation_by_period")
    private String riskCreationByPeriod;
	
    private String riskStatus;
    private String evidanceRequired;
    private String riskRegisterType; 
    private String supportingEvidance; 
    
    private String operation;
    @ManyToOne
    @JoinColumn(name="branchId")
    private Branch branch;
	
	@OneToMany(mappedBy = "riskHistory", cascade = CascadeType.ALL)
	private List<SubRiskHistory> subRiskHostory = new ArrayList<>();

}
