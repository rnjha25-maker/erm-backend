package ermorg.erm.service;

import java.util.List;

import ermorg.erm.constant.ErmStakeholderRole;
import ermorg.erm.dto.response.UserDto;
import ermorg.erm.dto.riskDTO.UserRequestDTO;
import ermorg.erm.model.User;

public interface IUserService {
	
	public User saveUser(UserRequestDTO userRequest);

	public List<UserDto> getUserList();

	public List<UserDto> getUsersByRole(ErmStakeholderRole role);
 
}
