package com.user_setup.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user_setup.dto.request.RoleRightDTO;
import com.user_setup.dto.response.GeneralResponse;
import com.user_setup.dto.response.ResponseStatus;
import com.user_setup.dto.response.RightResponse;
import com.user_setup.dto.response.RoleResponse;
import com.user_setup.exception.InvalidResourceAccess;
import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.service.IRightService;

@RestController
@RequestMapping("/right")
public class RightController {
	
	
	
	@Autowired
	private IRightService rightService;
	
	@PostMapping("/map")
	private GeneralResponse<RoleResponse> mapRight(@RequestBody RoleRightDTO request) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<RoleResponse> response = new GeneralResponse<>();
		
		RoleResponse roleRights = rightService.mapRoleRight(request);
		
		response.setData(roleRights);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role right mapped.");
		
		
		return response;
		
	}
	
	@PostMapping("/all//{id}")
	private GeneralResponse<List<RightResponse>> allRights(@PathVariable("id") Long organizationId ) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<List<RightResponse>> response = new GeneralResponse<>();
		
		List<RightResponse> allRights = rightService.getAllRights(organizationId);
		
		response.setData(allRights);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role right mapped.");
		
		
		return response;
		
	}
	

}
