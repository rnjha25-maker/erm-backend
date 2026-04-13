package com.erm.erm_command_organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="organization_module")
@NoArgsConstructor
@AllArgsConstructor
public class ModuleOrganization extends BaseModel{
	@ManyToOne
	@JoinColumn(name="org_id")
	private Organization organization;
	
	@Column(name="module_id")
	private Long moduleId;
	
	@Column(name="category_id")
	private Long categoryId;
	
	@Column(name="field_id")
	private Long fieldId;
	
	@Column(name="view_field_id")
	private Long viewFieldId;
	
	@Column(name="view_category_id")
	private Long viewCategoryId;

}
