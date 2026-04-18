package ermorg.org_setup_command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.Organization;
import ermorg.org_setup_command.repository.OrganizationRepository;


@Service
public class OrganizationService implements IOrganizationService {

	@Autowired
	private OrganizationRepository organizationRepository;
	public OrganizationService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Organization getOrganization(Long organizationId) throws ResourceNotFoundException {
		return organizationRepository.findById(organizationId).orElseThrow(()-> new ResourceNotFoundException("Organization not found."));
	}

}
