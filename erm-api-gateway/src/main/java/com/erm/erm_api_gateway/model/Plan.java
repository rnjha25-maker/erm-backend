package com.erm.erm_api_gateway.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Tbl_Plan")
public class Plan extends BaseModel {

	@Column(name = "plan_name")
	private String planName;

	@Column(name = "plan_description")
	private String planDescription;

}
