package com.user_setup.service;

import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.Organization;

public interface IOrganizationService {

	public Organization getOrganization(Long organizationId) throws ResourceNotFoundException;
}
