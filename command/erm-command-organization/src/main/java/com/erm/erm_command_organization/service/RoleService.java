package com.erm.erm_command_organization.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erm.erm_command_organization.dto.requestDTO.RoleRequest;
import com.erm.erm_command_organization.dto.responseDTO.RoleResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.Role;
import com.erm.erm_command_organization.model.history.RoleHistory;
import com.erm.erm_command_organization.repository.RoleRepository;
import com.erm.erm_command_organization.repository.history.RoleHistoryRepository;

@Service
public class RoleService implements IRoleService {

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private RoleHistoryRepository roleHistoryRepository;
	@Override
	public RoleResponse saveRole(RoleRequest roleRequest) {
		Role role;

		// Check if this is an update or create operation
		if (roleRequest.getRoleId() > 0) {
			role = roleRepository.findById(roleRequest.getRoleId())
					.filter(r -> !r.getDeleted())
					.orElseGet(Role::new);
		} else {
			role = new Role();
		}

		// Save history if updating existing role
		if (role.getId() != null && role.getId() > 0) {
			saveRoleHistory(role, "U");
		}

		// Set the role fields from the request
		role.setName(roleRequest.getRoleName());
		role.setPriority(roleRequest.getPriority());
		role.setDescription(roleRequest.getDescription());

		// Save the role
		Role savedRole = roleRepository.save(role);

		return new RoleResponse(savedRole);
	}
	@Override
	public RoleResponse getRole(long roleId) throws ResourceNotFoundException {
		Role role = roleRepository.findById(roleId).filter(r -> !r.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Role not found."));
		return new RoleResponse(role);
	}
	@Override
	public List<RoleResponse> getAllRoles() {
		return roleRepository.findAll().stream().filter(r -> !r.getDeleted() && !r.getName().equals("orgAdmin")).map(role -> new RoleResponse(role))
		.collect(Collectors.toList());
	}
	@Override
	public void deleteRole(long roleId) throws ResourceNotFoundException {
		Role role = roleRepository.findById(roleId).filter(r -> !r.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Role not found."));

		role.setDeleted(true);
		roleRepository.save(role);
		
		saveRoleHistory(role, "D");
		
	}
	
	private void saveRoleHistory(Role role, String operation) {
		
		RoleHistory roleHistory = new RoleHistory();
		roleHistory.setDeleted(role.getDeleted());
		roleHistory.setRoleId(role.getId());
		roleHistory.setRoleName(role.getName());
		roleHistory.setPriority(role.getPriority());
		roleHistory.setDescription(role.getDescription());
		roleHistory.setOperation(operation);
		
		roleHistoryRepository.save(roleHistory);
		
		
	}
	
	

}
