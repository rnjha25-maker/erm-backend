package com.erm.erm_query_organization.service;

import com.erm.erm_query_organization.dto.OrganizationDTO;
import com.erm.erm_query_organization.exception.DataNotFoundException;
import com.erm.erm_query_organization.model.Organization;

import java.util.List;

public interface IOrganizationService {
    Organization getOrganizationById(Long id) throws DataNotFoundException;
    List<OrganizationDTO> getAllOrganizations();
}
