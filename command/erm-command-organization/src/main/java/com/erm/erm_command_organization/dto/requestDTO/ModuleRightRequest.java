package com.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleRightRequest {

	@NotNull(message="organizationId must be provided.")
	private long organizationId;
	
	private List<ModuleRight> moduleRights = new ArrayList<>();

}
