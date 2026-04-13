package com.user_setup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.user_setup.dto.request.ComponyDTO;
import com.user_setup.exception.ResourceNotFoundException;
import com.user_setup.modal.Company;
import com.user_setup.modal.Organization;
import com.user_setup.repository.CompanyRepository;

@Service
public class CompanyService implements ICompanyService {

	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private IOrganizationService organizationService;

	@Override
	public Company getCompany(Long componyId) throws ResourceNotFoundException {
		return companyRepository.findById(componyId).orElseThrow(()-> new ResourceNotFoundException("Compony not found."));
	}

	@Override
	public void saveCompany(ComponyDTO request) throws ResourceNotFoundException {
		
		Organization organization = organizationService.getOrganization(request.getOrganizationId());
		
		Company company = new Company();
		
		company.setOrganization(organization);
		company.setCompanyLogoImageUrl(request.getLogo());
		company.setName(request.getComponyName());
		
		companyRepository.save(company);
		
	}

	@Override
	public void updateCompany(ComponyDTO request) throws ResourceNotFoundException {
		Company company = companyRepository.findById(request.getComponyId()).orElseThrow(()-> new ResourceNotFoundException("No company found"));
		
		company.setName(request.getComponyName());
		company.setCompanyLogoImageUrl(request.getLogo());
		
		companyRepository.save(company);
	}
	
	

}
