package com.erm.dto.response;

import com.erm.model.CustomField;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomResponse {

	private String fieldName;
	private String fieldType;
	private String value;
	
	public CustomResponse(CustomField customField) {
		this.fieldName = customField.getFieldName();
		this.fieldType = customField.getFieldType();
	}
}
