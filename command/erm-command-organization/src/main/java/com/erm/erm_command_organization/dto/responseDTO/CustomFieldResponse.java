package com.erm.erm_command_organization.dto.responseDTO;

import com.erm.erm_command_organization.model.CustomField;

import lombok.Data;

@Data
public class CustomFieldResponse {

	private Long id;
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private Long systemFieldId;
	private long fieldOrder;
	
	public CustomFieldResponse(CustomField customField) {
		this.id = customField.getId();
		this.fieldName = customField.getFieldName();
		this.fieldType = customField.getFieldType();
		this.required = customField.getRequired();
		this.systemFieldId = customField.getSystemField().getId();
		this.fieldOrder = customField.getFieldOrder();
				
		
	}

}
