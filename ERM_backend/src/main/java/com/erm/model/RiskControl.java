package com.erm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "risk_controls", indexes = {
	@jakarta.persistence.Index(name = "idx_riskcontrol_org_deleted", columnList  = "organization_id, deleted")
})
public class RiskControl extends BaseModel {

	@OneToMany(mappedBy = "riskControl")
	private List<SubRisk> subRisk = new ArrayList<>();
	
	@ManyToOne()
	@JoinColumn(name = "risk_id")
	private Risk risk;

	@Column(name = "control_source")
	private String controlSource;

	@Column(name = "control_title")
	private String controlTitle;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;

	@OneToMany(mappedBy = "riskControl")
	private List<RiskSubControl> subControls = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "control_tags", joinColumns = @JoinColumn(name = "control_id"))
	private List<String> tags;

	@Column(name = "control_type")
	private String controlType;

	@Column(name = "control_mechanism")
	private String controlMechanism;

	@Column(name = "access_controls")
	private String accessControls;

	@Column(name = "process_controls")
	private String processControls;

	@Column(name = "physical_controls")
	private String physicalControls;

	@Column(name = "technical_control")
	private String technicalControl;

	@Column(name = "control_assessment_frequency")
	private String controlAssessmentFrequency;

	@ManyToOne
	@JoinColumn(name = "primary_responsible_id")
	private User primaryResponsible;

	@ManyToOne
	@JoinColumn(name = "approver_id")
	private User approver;

	@Column(name = "actual_date")
	private Date actualDate;

	@Column(name = "last_control_assessment_date")
	private Date lastControlAssessmentDate;

	@Column(name = "next_control_assessment_date")
	private Date nextControlAssessmentDate;
}
