package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleListResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;

public interface ModuleService {
	
	public List<ModuleListResponse> getAllModules();

	public List<ModuleResponse> getAllModuleData();

}
