package com.erm.model.history;

import java.util.ArrayList;
import java.util.List;

import com.erm.constant.BusinessVertical;
import com.erm.constant.Functions;
import com.erm.constant.Impact;
import com.erm.constant.Likelihood;
import com.erm.constant.Period;
import com.erm.constant.Priority;
import com.erm.constant.RiskCategory;
import com.erm.constant.RiskSubCategory;
import com.erm.constant.Velocity;
import com.erm.model.BaseModel;
import com.erm.model.Branch;
import com.erm.model.Risk;
import com.erm.model.SubRisk;
import com.erm.model.User;

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
