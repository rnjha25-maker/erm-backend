package ermorg.org_setup_command.service;

import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.Organization;

public interface IOrganizationService {

	public Organization getOrganization(Long organizationId) throws ResourceNotFoundException;
}
