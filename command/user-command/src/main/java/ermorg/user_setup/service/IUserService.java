package ermorg.user_setup.service;

import java.util.List;

import ermorg.user_setup.constant.ErmStakeholderRole;
import ermorg.user_setup.dto.request.UserDTO;
import ermorg.user_setup.dto.response.UserResponse;
import ermorg.user_setup.exception.InvalidResourceAccess;
import ermorg.user_setup.exception.ResourceNotFoundException;

public interface IUserService {
	public UserResponse saveUser(UserDTO request) throws ResourceNotFoundException, InvalidResourceAccess;
	public UserResponse updateUser(UserDTO request) throws ResourceNotFoundException, InvalidResourceAccess;
	public UserResponse getUser(Long userId) throws ResourceNotFoundException, InvalidResourceAccess;
	public List<UserResponse> getAllUser(Long companyId) throws ResourceNotFoundException, InvalidResourceAccess;
	public List<UserResponse> getUsersByRole(Long companyId, ErmStakeholderRole role)
			throws ResourceNotFoundException, InvalidResourceAccess;

}
