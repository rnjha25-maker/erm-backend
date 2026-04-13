package com.erm.erm_command_organization.model.history;

import com.erm.erm_command_organization.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="right_organization_history")
public class RightOrganizationHistory extends BaseModel {
	
	private Long organizationId;
	private Long rightId;
	private String operation;

}
