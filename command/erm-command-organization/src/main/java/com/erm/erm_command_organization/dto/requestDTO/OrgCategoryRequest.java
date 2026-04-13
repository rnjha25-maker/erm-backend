package com.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class OrgCategoryRequest {

	private long categoryId;
	private long orgId;
	private List<Long> fieldIds;
}
