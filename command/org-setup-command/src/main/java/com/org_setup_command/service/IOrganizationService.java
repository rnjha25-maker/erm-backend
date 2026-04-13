package com.org_setup_command.service;

import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Organization;

public interface IOrganizationService {

	public Organization getOrganization(Long organizationId) throws ResourceNotFoundException;
}
