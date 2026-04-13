package com.user_setup.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user_setup.dto.request.RoleDTO;
import com.user_setup.dto.response.GeneralResponse;
import com.user_setup.dto.response.ResponseStatus;
import com.user_setup.dto.response.RoleResponse;
import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.service.IRoleService;

@RestController
@RequestMapping("role")
public class RoleController {

	@Autowired
	private IRoleService roleService;

	@PostMapping("/save")
	public GeneralResponse<RoleResponse> saveRole(RoleDTO request) throws ResourceNotFoundException {

		GeneralResponse<RoleResponse> response = new GeneralResponse<>();

		RoleResponse roleResponse = roleService.saveRole(request);

		response.setData(roleResponse);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role added.");

		return response;

	}

	
	@PutMapping("/update")
	public GeneralResponse<RoleResponse> updateRole(RoleDTO request) throws ResourceNotFoundException {

		GeneralResponse<RoleResponse> response = new GeneralResponse<>();

		RoleResponse roleResponse = roleService.updateRole(request);

		response.setData(roleResponse);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role updated.");

		return response;

	}

	@DeleteMapping("/delete/{roleId}")
	public GeneralResponse<RoleResponse> deleteRole(@PathVariable("roleId") Long id) throws ResourceNotFoundException {

		GeneralResponse<RoleResponse> response = new GeneralResponse<>();

		roleService.deleteRole(id);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role updated.");

		return response;

	}
	
	@GetMapping("/all/{companyId}")
	public GeneralResponse<List<RoleResponse>> getAllRoles(@PathVariable("companyId") Long companyId) throws ResourceNotFoundException {

		GeneralResponse<List<RoleResponse>> response = new GeneralResponse<>();

		List<RoleResponse> roles = roleService.getAllRoles(companyId);
		response.setData(roles);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role updated.");

		return response;

	}
	
	@GetMapping("/{roleId}")
	public GeneralResponse<RoleResponse> getRole(@PathVariable("roleId") Long roleId) throws ResourceNotFoundException {

		GeneralResponse<RoleResponse> response = new GeneralResponse<>();

		RoleResponse role = roleService.getRole(roleId);
		response.setData(role);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role updated.");

		return response;

	}
	
	

}
