package com.erm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrgFields extends BaseModel{

	@ManyToOne
	@JoinColumn(name="org_id")
	private Organization organization;
	
	private String tabName;
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String mappedWith;
	
	
	
	
}
