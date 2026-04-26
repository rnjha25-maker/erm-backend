package ermorg.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleListResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.service.IModuleService;

//@CrossOrigin
@RestController
@RequestMapping("module")
public class ModuleController {
	
	@Autowired
	private IModuleService moduleService;
	
	@GetMapping("/all")
	public GeneralResponse<List<ModuleListResponse>> getAllModules(){
		GeneralResponse<List<ModuleListResponse>> response = new GeneralResponse<>();
		List<ModuleListResponse> allModules = moduleService.getAllModules();
		
		response.setData(allModules);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/all-module-data")
	public GeneralResponse<List<ModuleResponse>> getAllModuleData(){
		GeneralResponse<List<ModuleResponse>> response = new GeneralResponse<>();
		List<ModuleResponse> allModules = moduleService.getAllModuleData();
		
		response.setData(allModules);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}

}
