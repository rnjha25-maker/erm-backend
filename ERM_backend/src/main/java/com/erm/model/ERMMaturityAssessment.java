package com.erm.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "erm_maturity_assessments", indexes = {
	@jakarta.persistence.Index(name = "idx_ermaturity_org_deleted", columnList  = "organization_id, deleted")
})
@Entity
public class ERMMaturityAssessment extends BaseModel {

	@Column(name = "assessment_area_name")
	private String assessmentAreaName;

	@Column(name = "assessment_area_id")
	private Long assessmentAreaId;

	@Column(name = "key_assessment_parameters")
	private String keyAssessmentParameters;

	@Column(name = "status")
	private String status;

	@Column(name = "weightage_score")
	private BigDecimal weightageScore;

	@Column(name = "marks_achieved")
	private String marksAchieved;

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
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

}
