package com.erm.erm_command_organization.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RightDto {

	@NotBlank(message="Right name must be provided")
	private String rightName;
	private String rightDescription;
	private long rightId;
	@NotNull(message="Please select module.")
	private long moduleId;
	
}
