package com.erm.service;

import java.util.List;

import com.erm.dto.response.ModuleListResponse;
import com.erm.exception.ResourceNotFoundException;

public interface ModuleService {
	
	public List<ModuleListResponse> getAllModules() throws ResourceNotFoundException;

}
