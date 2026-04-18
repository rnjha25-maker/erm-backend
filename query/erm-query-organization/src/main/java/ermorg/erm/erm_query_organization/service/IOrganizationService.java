package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.dto.OrganizationDTO;
import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Organization;

import java.util.List;

public interface IOrganizationService {
    Organization getOrganizationById(Long id) throws DataNotFoundException;
    List<OrganizationDTO> getAllOrganizations();
}
