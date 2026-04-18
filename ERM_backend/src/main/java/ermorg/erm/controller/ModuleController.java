package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.ModuleListResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.ModuleService;

@RestController
@RequestMapping("/module")
public class ModuleController {
	
	@Autowired
	private ModuleService moduleService;
	
	@GetMapping("/all")
	public GeneralResponse<List<ModuleListResponse>> getAllModules() throws ResourceNotFoundException{
		GeneralResponse<List<ModuleListResponse>> response = new GeneralResponse<>();
		List<ModuleListResponse> allModules = moduleService.getAllModules();
		
		response.setData(allModules);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	

}
