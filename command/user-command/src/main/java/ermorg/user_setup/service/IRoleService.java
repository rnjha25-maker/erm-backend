package ermorg.user_setup.service;

import java.util.List;

import ermorg.user_setup.dto.request.RoleDTO;
import ermorg.user_setup.dto.response.RoleResponse;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Role;

public interface IRoleService {

	public RoleResponse getRole(Long roleId) throws ResourceNotFoundException;

	public RoleResponse saveRole(RoleDTO request) throws ResourceNotFoundException;

	RoleResponse updateRole(RoleDTO request) throws ResourceNotFoundException;

	public void deleteRole(Long id) throws ResourceNotFoundException;

	public List<RoleResponse> getAllRoles(Long companyId) throws ResourceNotFoundException;
}
