package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IRoleRightMappingService {
	
	public RoleRightMappingResponse map(RoleRightMappingRequest roleRightMappingRequest) throws ResourceNotFoundException;

	public RoleRightMappingResponse getMapping(Long roleId);

}
