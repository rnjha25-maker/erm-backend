package com.erm.service;

import java.util.List;

import com.erm.constant.ErmStakeholderRole;
import com.erm.dto.response.UserDto;
import com.erm.dto.riskDTO.UserRequestDTO;
import com.erm.model.User;

public interface IUserService {
	
	public User saveUser(UserRequestDTO userRequest);

	public List<UserDto> getUserList();

	public List<UserDto> getUsersByRole(ErmStakeholderRole role);
 
}
