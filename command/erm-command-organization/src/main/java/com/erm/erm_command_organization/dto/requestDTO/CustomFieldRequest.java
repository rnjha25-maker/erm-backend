package com.erm.erm_command_organization.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomFieldRequest {

	private long fieldId;
	@NotBlank(message="Field Name must be provided.")
	private String fieldName;
	@NotBlank(message="Field Type must be provided.")
	private String fieldType;
	@NotNull(message="Please select Mapped With Column.")
	private long mappedWithColumnId;
	
	private boolean required;
	
	private String fieldBehavior;
	
	private boolean showGridColumn;
	
	private boolean showInView;
	
	private boolean disabled;
	
	private int fieldOrder;
}
