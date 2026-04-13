package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ViewFieldRequest {
	
	@NotBlank(message="View Category must be provided.")
	private String viewCategory;
	private long viewCategoryId;
	private String viewName;
	
	@NotNull(message="Please select View")
	private long viewId;
	
	@NotNull(message="Please select Module")
	@Min(value=1, message="Please select Module")
	private long moduleId;
	
	private List<CustomViewFieldRequest> fields;

}
