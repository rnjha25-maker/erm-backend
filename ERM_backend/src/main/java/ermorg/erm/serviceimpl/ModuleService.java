package ermorg.erm.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.ModuleListResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.ModuleOrganization;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.ModuleOrganizationRepository;
import ermorg.erm.repository.ModuleRepository;
import ermorg.erm.service.IModuleService;
import ermorg.erm.util.OrganizationContext;
@Service
public class ModuleService implements IModuleService {

	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private ModuleOrganizationRepository moduleOrganizationRepository;
	
	@Override
	@Transactional(readOnly = true)
	public List<ModuleListResponse> getAllModules() throws ResourceNotFoundException{
		
		Organization organization = OrganizationContext.getOrganization();
		
		if(organization == null) {
			throw new ResourceNotFoundException("Organization not found");

		}
		
		List<ModuleOrganization> organizationModules = moduleOrganizationRepository.getByOrganizationId(organization.getId());
		List<Long> moduleIds = organizationModules.stream().filter(module->!module.getDeleted())
		.map(module -> module.getModuleId())
		.collect(Collectors.toList());
		
		return moduleRepository.findAllById(moduleIds)
				.stream().filter(module->!module.getDeleted())
				.map(module -> new ModuleListResponse(module))
				.collect(Collectors.toList());
		
	}
	

}
