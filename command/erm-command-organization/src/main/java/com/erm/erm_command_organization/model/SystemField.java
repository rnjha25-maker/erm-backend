package com.erm.erm_command_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="system_fields")
@ToString
public class SystemField extends BaseModel{

	private String field;
	
	@ManyToOne
	@JoinColumn(name="system_table_id")
	private SystemTable systemTable;
}
