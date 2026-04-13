package com.erm.erm_command_organization.dto.requestDTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RoleRightMappingRequest {
	private long roleId;
	
	private List<RightMappingRequest> rights = new ArrayList<>();

}
