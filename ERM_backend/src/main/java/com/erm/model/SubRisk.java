package com.erm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubRisk extends BaseModel{
	
    private String subRisk;
    private String riskSubCategory;
//    @ManyToOne
//    private User user;
    @ManyToOne
    @JoinColumn(name = "risk_id")
    private Risk risk;
    
//    @ManyToOne
//    @JoinColumn(name = "organization_id")
//    private Organization organization;
    
    
    @Column(name = "organization_id")
    private Long organizationId;
    
//    @ManyToOne
//    @JoinColumn(name = "company_id")
//    private Company company;
    
    
    @Column(name = "company_id")
    private Long companyId;
    
    @ManyToOne
    @JoinColumn(name = "risk_control_id")
    private RiskControl riskControl;
	    
    @ManyToOne
    @JoinColumn(name = "risk_response_treatment_id")
    private RiskResponseTreatment riskResponseTreatment;
    
    @ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "risk_review_id")
    private RiskReview riskReview;
    
    @ManyToOne
	@JoinColumn(name = "assessment_id")
    private RiskAssessment riskAssessment;
    
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name = "kri_kpi_review_id")
	private KriKpiReview kriKpiReview;
    
}
