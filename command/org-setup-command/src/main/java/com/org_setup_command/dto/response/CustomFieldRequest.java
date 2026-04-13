package com.org_setup_command.dto.response;

import lombok.Data;

@Data
public class CustomFieldRequest {

	private long fieldId;
	private String fieldName;
	private String fieldType;
	private String mappedWith;
	private long orgId;
	private boolean required;
}
