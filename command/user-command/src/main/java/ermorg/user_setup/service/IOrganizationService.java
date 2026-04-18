package ermorg.user_setup.service;

import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Organization;

public interface IOrganizationService {

	public Organization getOrganization(Long organizationId) throws ResourceNotFoundException;
}
