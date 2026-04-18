package ermorg.user_setup.service;

import java.util.List;

import ermorg.user_setup.dto.request.RoleRightDTO;
import ermorg.user_setup.dto.response.RightResponse;
import ermorg.user_setup.dto.response.RoleResponse;
import ermorg.user_setup.exception.InvalidResourceAccess;
import ermorg.user_setup.exception.ResourceNotFoundException;

public interface IRightService {
	
	public List<RightResponse> getAllRights(Long companyId) throws ResourceNotFoundException, InvalidResourceAccess;
	public RoleResponse mapRoleRight(RoleRightDTO request)throws ResourceNotFoundException, InvalidResourceAccess;

}
