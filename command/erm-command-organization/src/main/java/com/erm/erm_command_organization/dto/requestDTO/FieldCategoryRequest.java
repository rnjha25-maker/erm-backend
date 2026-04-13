package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class FieldCategoryRequest {

	private long orgId;
	private List<Long> cutomFieldIds;
}
