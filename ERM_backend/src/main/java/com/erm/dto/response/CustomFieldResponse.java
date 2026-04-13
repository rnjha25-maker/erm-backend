package com.erm.dto.response;

import com.erm.model.CustomField;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomFieldResponse {

	private Long id;
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String systemFieldName;
	private String fieldBehavior;
	
	private Boolean showGridColumn;
	
	private Boolean showInView;
	
	private Boolean disabled;
	
	private Integer fieldOrder;
	private String value;
	public CustomFieldResponse(CustomField customField) {
		this.id = customField.getId();
		this.fieldName = customField.getFieldName();
		this.fieldType = customField.getFieldType();
		this.required = customField.getRequired();
		this.systemFieldName = customField.getSystemField().getField();
		this.fieldBehavior = customField.getFieldBehavior();
		this.showGridColumn = customField.getShowGridColumn();
		this.showInView = customField.getShowInView();
		this.disabled = customField.getDisabled();
		this.fieldOrder = customField.getFieldOrder();
		
	}

}
