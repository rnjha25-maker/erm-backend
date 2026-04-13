package com.erm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "risk_sub_control")
public class RiskSubControl extends BaseModel {

	private String subControlTitle;
	
	@ManyToOne
	@JoinColumn(name = "risk_control_id")
	private RiskControl riskControl;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private Company company;
}
