package ermorg.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.repository.OrganizationRepository;

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
