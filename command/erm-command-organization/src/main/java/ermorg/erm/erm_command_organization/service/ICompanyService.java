package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.requestDTO.companyDTO.AddRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.CompanyResponse;
import ermorg.erm.erm_command_organization.exception.LimitExceedException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface ICompanyService {
	CompanyResponse createCompany(AddRequest request) throws ResourceNotFoundException, LimitExceedException;
	CompanyResponse updateCompany(AddRequest request) throws ResourceNotFoundException;
    void deleteCompany(Long id) throws ResourceNotFoundException;
	CompanyResponse getCompany(Long companyId)throws ResourceNotFoundException;
	List<CompanyResponse> getAllCompanies(Long organizationId) throws ResourceNotFoundException;;
}
