package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IRoleService {
	
	public RoleResponse saveRole(RoleRequest roleRequest);
	
	public RoleResponse getRole(long roleId) throws ResourceNotFoundException;
	
	public List<RoleResponse> getAllRoles();
	
	public void deleteRole(long roleId) throws ResourceNotFoundException;

}
