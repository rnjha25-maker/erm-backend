package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleListResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import ermorg.erm.erm_command_organization.model.Modules;
import ermorg.erm.erm_command_organization.repository.ModuleRepository;
import ermorg.erm.erm_command_organization.service.IModuleService;
@Service
public class ModuleService implements IModuleService {

	@Autowired
	private ModuleRepository moduleRepository;
	@Override
	public List<ModuleListResponse> getAllModules() {
		return moduleRepository.findAll()
				.stream()
				.filter(module1 -> !module1.getDeleted())
				.map(module-> new ModuleListResponse(module))
				.collect(Collectors.toList());
		
	}
	@Override
	public List<ModuleResponse> getAllModuleData() {
		List<Modules> modules = moduleRepository.findAll();
		
		return modules.stream()
		.map(module -> new ModuleResponse(module))
		.collect(Collectors.toList());
	}
	
	

}
