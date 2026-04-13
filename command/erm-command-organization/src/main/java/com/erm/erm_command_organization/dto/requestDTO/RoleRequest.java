package com.erm.erm_command_organization.dto.requestDTO;

import lombok.Data;

@Data
public class RoleRequest {
	
	private long roleId;
	private String roleName;
	private long priority;
	private String description;

}
