package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import com.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;

public interface IRoleRightMappingService {
	
	public RoleRightMappingResponse map(RoleRightMappingRequest roleRightMappingRequest);

	public RoleRightMappingResponse getMapping(Long roleId);

}
