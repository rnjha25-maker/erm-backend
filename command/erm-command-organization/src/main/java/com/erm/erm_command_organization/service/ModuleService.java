package com.erm.erm_command_organization.service;

import java.util.List;

import com.erm.erm_command_organization.dto.responseDTO.ModuleListResponse;
import com.erm.erm_command_organization.dto.responseDTO.ModuleResponse;

public interface ModuleService {
	
	public List<ModuleListResponse> getAllModules();

	public List<ModuleResponse> getAllModuleData();

}
