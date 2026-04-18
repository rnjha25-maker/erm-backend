package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.ModuleListResponse;
import ermorg.erm.exception.ResourceNotFoundException;

public interface ModuleService {
	
	public List<ModuleListResponse> getAllModules() throws ResourceNotFoundException;

}
