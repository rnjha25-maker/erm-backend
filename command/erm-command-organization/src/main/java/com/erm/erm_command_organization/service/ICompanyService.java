package com.erm.erm_command_organization.service;

import java.util.List;

import com.erm.erm_command_organization.dto.requestDTO.companyDTO.AddRequest;
import com.erm.erm_command_organization.dto.responseDTO.CompanyResponse;
import com.erm.erm_command_organization.exception.LimitExceedException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface ICompanyService {
	CompanyResponse createCompany(AddRequest request) throws ResourceNotFoundException, LimitExceedException;
	CompanyResponse updateCompany(AddRequest request) throws ResourceNotFoundException;
    void deleteCompany(Long id) throws ResourceNotFoundException;
	CompanyResponse getCompany(Long companyId)throws ResourceNotFoundException;
	List<CompanyResponse> getAllCompanies(Long organizationId) throws ResourceNotFoundException;;
}
