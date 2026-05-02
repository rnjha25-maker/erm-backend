package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.ModuleListResponse;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IModuleService {
	
	public List<ModuleListResponse> getAllModules() throws ResourceNotFoundException;

}
