package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;

public interface IRoleRightMappingService {
	
	public RoleRightMappingResponse map(RoleRightMappingRequest roleRightMappingRequest);

	public RoleRightMappingResponse getMapping(Long roleId);

}
