package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class UpdateModuleRequest {

	private long orgId;
	private long moduleId;
	private List<FieldCategoryRequest> fieldCategories;
}
