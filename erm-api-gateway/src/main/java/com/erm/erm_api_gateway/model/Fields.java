package com.erm.erm_api_gateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fields")
public class Fields extends BaseModel {

	private String tabName;
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String mappedWith;

	@ManyToOne
	@JoinColumn(name = "module_id")
	private Modules module;

}
