package com.erm.erm_command_organization.model.history;

import com.erm.erm_command_organization.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "right_history")
public class RightHistory extends BaseModel {
	private String rightName;
	private String description;
	private long rightId;
	private long moduleId;
	private String operation;

}
