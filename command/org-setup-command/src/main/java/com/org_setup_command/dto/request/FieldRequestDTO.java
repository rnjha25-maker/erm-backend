package com.org_setup_command.dto.request;

import java.util.List;

import com.org_setup_command.dto.response.CustomFieldRequest;

import lombok.Data;

@Data
public class FieldRequestDTO {
	
	private String category;
	private String tableName;
	private long moduleId;
	
	List<CustomFieldRequest> fields;
	

}
