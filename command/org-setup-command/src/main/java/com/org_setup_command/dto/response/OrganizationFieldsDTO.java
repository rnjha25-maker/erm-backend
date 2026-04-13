package com.org_setup_command.dto.response;

import com.org_setup_command.modal.CustomField;

import lombok.Data;

@Data
public class OrganizationFieldsDTO {
	private long fieldId;
	private String fieldName;
	private String mappedWith;
	private long moduleId;
	private String moduleName;
	private String fieldType;
	private boolean isRequired;
	private String tabName;
	public OrganizationFieldsDTO(CustomField fields) {
		
		this.fieldId = fields.getId();
		this.fieldName = fields.getFieldName();
		this.mappedWith = fields.getMappedWith();
//		this.moduleName = fields.getTabName();
		this.fieldType = fields.getFieldType();
		this.isRequired = fields.getRequired();
//		this.tabName = fields.getTabName();
	}
	
	
	

}
