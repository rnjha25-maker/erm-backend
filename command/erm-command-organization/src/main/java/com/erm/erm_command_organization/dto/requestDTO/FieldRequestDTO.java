package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldRequestDTO {

	@NotBlank(message="Category must be provided.")
	private String category;
	private long categoryId;
	
	private String tableName;
	@NotNull(message="Please select table.")
	private long tableId;
	
	private String moduleName;
	@NotNull(message="Please select module.")
	@Min(value=1, message="Please select module.")
	private long moduleId;
	
	List<CustomFieldRequest> fields;
	
}
