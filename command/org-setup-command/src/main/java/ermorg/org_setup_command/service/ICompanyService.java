package ermorg.org_setup_command.service;

import java.util.List;

import ermorg.org_setup_command.dto.request.CompanyDTO;
import ermorg.org_setup_command.dto.response.CompanyResponse;
import ermorg.org_setup_command.exception.ResourceNotFoundException;

public interface ICompanyService {
	CompanyResponse createCompany(CompanyDTO request) throws ResourceNotFoundException;
	CompanyResponse updateCompany(CompanyDTO request) throws ResourceNotFoundException;
	CompanyResponse deleteCompany(Long id) throws ResourceNotFoundException;
	CompanyResponse getCompany(Long companyId) throws ResourceNotFoundException;
	List<CompanyResponse> getAllCompany(Long companyId) throws ResourceNotFoundException;
}
