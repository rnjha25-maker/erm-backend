package com.erm.erm_command_organization.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.erm.erm_command_organization.dto.requestDTO.companyDTO.AddRequest;
import com.erm.erm_command_organization.dto.responseDTO.CompanyResponse;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.LimitExceedException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.City;
import com.erm.erm_command_organization.model.Company;
import com.erm.erm_command_organization.model.Country;
import com.erm.erm_command_organization.model.Organization;
import com.erm.erm_command_organization.model.State;
import com.erm.erm_command_organization.model.history.CompanyHistory;
import com.erm.erm_command_organization.repository.CompanyRepository;
import com.erm.erm_command_organization.repository.CountryRepository;
import com.erm.erm_command_organization.repository.OrganizationRepository;
import com.erm.erm_command_organization.repository.history.CompanyHistoryRepository;
import com.erm.erm_command_organization.util.AuditorAwareImpl;

@Service
public class CompanyService implements ICompanyService {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository; 
    
    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private CompanyHistoryRepository companyHistoryRepository;

    @Autowired
    private AuditorAware auditor;

    @Override
    public CompanyResponse createCompany(AddRequest request) throws ResourceNotFoundException, LimitExceedException{
    	Organization organization = organizationRepository.findById(request.getOrganizationId())
    	.filter(org -> !org.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("No organization found."));
    	
    	Country country = countryRepository.findById(request.getCountryId()).orElseThrow(() -> new ResourceNotFoundException("No country found."));
    	
    	State state = country.getStates().stream().filter(s1 -> s1.getId().equals(request.getStateId())).findAny().orElseThrow(() -> new ResourceNotFoundException("No state found."));
    	
    	City city = state.getCities().stream().filter(c1 -> c1.getId().equals(request.getCityId())).findAny().orElseThrow(() -> new ResourceNotFoundException("No city found."));
    	
    	if(organization.getCompanyCount()== null || organization.getCompanyCount()<=organization.getCompanies().size()) throw new LimitExceedException("Company limit exceed. No more company can be added.");
    	
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Company company = new Company();
        company.setOrganization(organization);
		company.setCountry(country);
		company.setState(state);
		company.setCity(city);
        company.setName(request.getCompanyName());
        company.setCompanyLogoImageUrl(request.getCompanyLogoImageUrl());
        company.setClientIP(clientIp);
        company.setAddress(request.getAddress());
        company.setCompanySite(request.getCompanySite());
        company.setPincode(request.getPincode());
        Company savedCompany = companyRepository.save(company);
        
        organization.getCompanies().add(savedCompany);
        organizationRepository.save(organization);
        
        return new CompanyResponse(savedCompany);
    }

    @Override
    public CompanyResponse updateCompany(AddRequest request) throws  ResourceNotFoundException{
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
        Company company = companyRepository.findById(request.getCompanyId()).filter(c -> !c.getDeleted()).orElseThrow(() -> new DataNotFoundException("Invalid Company ID."));

        Country country = countryRepository.findById(request.getCountryId()).orElseThrow(() -> new ResourceNotFoundException("No country found."));
    	
    	State state = country.getStates().stream().filter(s1 -> s1.getId().equals(request.getStateId())).findAny().orElseThrow(() -> new ResourceNotFoundException("No state found."));
    	
    	City city = state.getCities().stream().filter(c1 -> c1.getId().equals(request.getCityId())).findAny().orElseThrow(() -> new ResourceNotFoundException("No city found."));

		company.setCountry(country);
		company.setState(state);
		company.setCity(city);
        company.setName(request.getCompanyName());
        company.setCompanyLogoImageUrl(request.getCompanyLogoImageUrl());
        company.setClientIP(clientIp);
        company.setAddress(request.getAddress());
        company.setCompanySite(request.getCompanySite());
        company.setPincode(request.getPincode());
        
        saveCompanyHistory(company, "U");

        return new CompanyResponse(companyRepository.save(company));
        
    }

    @Override
    public void deleteCompany(Long id) throws ResourceNotFoundException{ 
        Company company = companyRepository.findById(id).filter(c -> !c.getDeleted()).orElseThrow(() -> new InvalidDataException("Invalid Company ID."));
        company.setDeleted(true);
        companyRepository.save(company);
        
        saveCompanyHistory(company, "D");
    }
    
    
    @Override
	public CompanyResponse getCompany(Long companyId) throws ResourceNotFoundException {
		
    	Company company = companyRepository.findById(companyId).filter(c -> !c.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Invalid Company ID."));
		return new CompanyResponse(company);
	}

    
	@Override
	public List<CompanyResponse> getAllCompanies(Long organizationId) throws ResourceNotFoundException {
		
		if(organizationId == 0) return companyRepository.findAll().stream().filter(c -> !c.getDeleted()).map(CompanyResponse::new).collect(Collectors.toList());
		
		Organization organization = organizationRepository.findById(organizationId).filter(o -> !o.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("Invalid Organization ID."));
		
		return organization.getCompanies().stream().filter(c -> !c.getDeleted()).map(CompanyResponse::new).collect(Collectors.toList());
	
	}

	private void saveCompanyHistory(Company company, String operation) {
    	CompanyHistory companyHistory = new CompanyHistory();
		companyHistory.setOperation(operation);
		companyHistory.setName(company.getName());
		companyHistory.setClientIP(((AuditorAwareImpl) auditor).getClientIp());
		companyHistory.setOrganizationId(company.getOrganization().getId());
		companyHistory.setCompanyId(company.getId());
		companyHistory.setName(company.getName());
		companyHistory.setCompanyLogoImageUrl(company.getCompanyLogoImageUrl());
		companyHistory.setCompanySite(company.getCompanySite());
		companyHistory.setPincode(company.getPincode());
		companyHistory.setAddress(company.getAddress());
		companyHistory.setStatus(company.getStatus());
		companyHistory.setCountryId(company.getCountry().getId());
		companyHistory.setCityId(company.getCity().getId());
		companyHistory.setStateId(company.getState().getId());
		companyHistoryRepository.save(companyHistory);
    }
}
