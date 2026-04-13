package com.user_setup.service;

import java.util.List;

import com.user_setup.dto.request.RoleRightDTO;
import com.user_setup.dto.response.RightResponse;
import com.user_setup.dto.response.RoleResponse;
import com.user_setup.exception.InvalidResourceAccess;
import com.user_setup.exception.ResourceNotFoundException;

public interface IRightService {
	
	public List<RightResponse> getAllRights(Long companyId) throws ResourceNotFoundException, InvalidResourceAccess;
	public RoleResponse mapRoleRight(RoleRightDTO request)throws ResourceNotFoundException, InvalidResourceAccess;

}
