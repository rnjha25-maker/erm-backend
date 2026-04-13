package com.erm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erm.constant.ErmStakeholderRole;
import com.erm.dto.response.UserDto;
import com.erm.dto.riskDTO.UserRequestDTO;
import com.erm.model.Company;
import com.erm.model.User;
import com.erm.repository.UserRepository;
import com.erm.util.CompanyContext;

@Service
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	@Override
	public User saveUser(UserRequestDTO userRequest) {
		
		User user = new User();
		BeanUtils.copyProperties(userRequest, user, "id");
		
		System.out.println(user.getId() +" "+ user.getEmail());
		
		return userRepository.save(user);
	}

	@Override
	public List<UserDto> getUserList() {
		Company company = CompanyContext.getCompany();
		
		if(company == null) return null;
		
		return userRepository.getUsersByCompany(company.getId())
		.stream()
		.filter(u -> !u.getDeleted())
		.map(u -> new UserDto(u))
		.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	@Override
	public List<UserDto> getUsersByRole(ErmStakeholderRole role) {
		Company company = CompanyContext.getCompany();
		if (company == null) {
			return null;
		}
		return userRepository.findByCompanyIdAndRoleNameIgnoreCase(company.getId(), role.getDisplayName()).stream()
				.filter(u -> !u.getDeleted())
				.map(UserDto::new)
				.collect(Collectors.toList());
	}

}
