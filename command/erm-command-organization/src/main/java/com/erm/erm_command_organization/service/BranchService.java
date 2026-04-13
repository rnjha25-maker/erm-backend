package com.erm.erm_command_organization.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.erm.erm_command_organization.dto.requestDTO.BranchRequest;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponse;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponseDto;
import com.erm.erm_command_organization.exception.DataNotFoundException;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.model.Branch;
import com.erm.erm_command_organization.model.City;
import com.erm.erm_command_organization.model.Company;
import com.erm.erm_command_organization.model.Country;
import com.erm.erm_command_organization.model.Department;
import com.erm.erm_command_organization.model.Organization;
import com.erm.erm_command_organization.model.State;
import com.erm.erm_command_organization.model.history.BranchHistory;
import com.erm.erm_command_organization.repository.AddressRepository;
import com.erm.erm_command_organization.repository.BranchRepository;
import com.erm.erm_command_organization.repository.CompanyRepository;
import com.erm.erm_command_organization.repository.CountryRepository;
import com.erm.erm_command_organization.repository.OrganizationRepository;
import com.erm.erm_command_organization.repository.history.BranchHistoryRepository;
import com.erm.erm_command_organization.util.AuditorAwareImpl;

@Service
public class BranchService implements IBranchService {

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private AuditorAware auditor;

	@Autowired
	private CountryRepository countryRepository;
	
	@Autowired
	private BranchHistoryRepository branchHistoryRepository;

	@Override
	public BranchResponse createBranch(BranchRequest request) throws ResourceNotFoundException {

		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.filter(o -> !o.getDeleted()).orElseThrow(() -> new DataNotFoundException("Organization not found."));

		Company company = organization.getCompanies().stream().filter(c -> c.getId().equals(request.getCompanyId()))
				.findAny().orElseThrow(() -> new DataNotFoundException("Company not found."));
		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

		List<Country> countries = countryRepository.findAll();
		List<Branch> branches = request.getBranches().stream().map(branchRequest -> {
			Branch branch = new Branch();

			branch.setClientIP(clientIp);
			branch.setName(branchRequest.getBranchName());
			branch.setDescription(branchRequest.getDescription());
			branch.setType(branchRequest.getType());
			branch.setAddress(branchRequest.getAddress());
			branch.setCompany(company);
			branch.setOrganization(organization);

			List<Department> departments = organization.getDepartments().stream()
					.filter(department -> branchRequest.getDepartmentIds().contains(department.getId()))
					.collect(Collectors.toList());

			branch.setDepartments(departments);

			Country country = countries.stream().filter(c -> c.getId().equals(branchRequest.getCountryId())).findAny()
					.orElse(null);

			State state = null;
			if (country != null) {
				state = country.getStates().stream().filter(s -> s.getId().equals(branchRequest.getStateId())).findAny()
						.orElse(null);
			}
			City city = null;
			if (state != null) {

				city = state.getCities().stream().filter(c -> c.getId().equals(branchRequest.getCityId())).findAny()
						.orElse(null);
			}

			branch.setCountry(country);
			branch.setState(state);
			branch.setCity(city);
			branch.setPincode(branchRequest.getPincode());
			branch.setAddress(branchRequest.getAddress());
			branch.setDeleted(false);
			return branch;
		}).collect(Collectors.toList());

		List<Branch> savedBranches = branchRepository.saveAll(branches);

		return new BranchResponse(savedBranches, organization.getId(), company.getId());
	}

	@Override
	public BranchResponse updateBranch(BranchRequest request) throws ResourceNotFoundException {

		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.filter(o -> !o.getDeleted()).orElseThrow(() -> new DataNotFoundException("Organization not found."));

		Company company = organization.getCompanies().stream().filter(c -> c.getId().equals(request.getCompanyId()))
				.findAny().orElseThrow(() -> new DataNotFoundException("Company not found."));

		List<Country> countries = countryRepository.findAll();

		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

		List<BranchHistory> branchHistories = new ArrayList<>();
		for (Branch branch : company.getBranches()) {

			request.getBranches().stream().filter(branchRequest -> branchRequest.getBranchId() == branch.getId())
			.findFirst().ifPresent(branchRequest -> {
				
				BranchHistory branchHistory = new BranchHistory();
				branchHistory.setClientIP(clientIp);
				branchHistory.setName(branch.getName());
				branchHistory.setDescription(branch.getDescription());
				branchHistory.setOperation("U");
				branchHistory.setPincode(branch.getPincode());
				branchHistory.setType(branch.getType());
				branchHistory.setStatus(branch.getStatus());
				
				branchHistory.setBranchId(branch.getId());
				branchHistory.setOrganizationId(organization.getId());
				branchHistory.setCompanyId(company.getId());
				branchHistory.setCityId(branch.getCity() != null ? branch.getCity().getId() :null);
				branchHistory.setStateId(branch.getState() != null ? branch.getState().getId() : null);
				branchHistory.setCountryId(branch.getCountry() != null ? branch.getCountry().getId() : null);
				branchHistory.setAddress(branch.getAddress());
				branchHistories.add(branchHistory);
				
				List<Department> departments = organization.getDepartments().stream()
						.filter(department -> branchRequest.getDepartmentIds().contains(department.getId()))
						.collect(Collectors.toList());

				branch.setDepartments(departments);

				Country country = countries.stream().filter(c -> c.getId().equals(branchRequest.getCountryId())).findAny()
						.orElse(null);

				State state = null;
				if (country != null) {
					state = country.getStates().stream().filter(s -> s.getId().equals(branchRequest.getStateId())).findAny()
							.orElse(null);
				}
				City city = null;
				if (state != null) {

					city = state.getCities().stream().filter(c -> c.getId().equals(branchRequest.getCityId())).findAny()
							.orElse(null);
				}

				branch.setClientIP(clientIp);
				branch.setName(branchRequest.getBranchName());
				branch.setDescription(branchRequest.getDescription());
				branch.setType(branchRequest.getType());
				branch.setAddress(branchRequest.getAddress());
				branch.setCompany(company);
				branch.setOrganization(organization);
				branch.setCountry(country);
				branch.setState(state);
				branch.setCity(city);
				branch.setPincode(branchRequest.getPincode());
				branch.setAddress(branchRequest.getAddress());
				branch.setDeleted(false);
				
			});
			
			
		}
		
		List<Branch> savedBranches = branchRepository.saveAll(company.getBranches());
		
		saveBranchHistory(branchHistories);
		return new BranchResponse(savedBranches, organization.getId(), company.getId());
	}

	private void saveBranchHistory(List<BranchHistory> branchHistories) {
		
		branchHistoryRepository.saveAll(branchHistories);
		
		
	}

	@Override
	public void deleteBranch(Long id) throws InvalidDataException {
		
		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

		Branch branch = branchRepository.findById(id).filter(b -> !b.getDeleted()).orElseThrow(() -> new InvalidDataException("No branch found"));
		
		branch.setDeleted(true);
		branch.setClientIP(clientIp);
		branchRepository.save(branch);
		
		BranchHistory branchHistory = new BranchHistory();
		branchHistory.setClientIP(clientIp);
		branchHistory.setName(branch.getName());
		branchHistory.setDescription(branch.getDescription());
		branchHistory.setOperation("U");
		branchHistory.setPincode(branch.getPincode());
		branchHistory.setType(branch.getType());
		branchHistory.setStatus(branch.getStatus());
		
		branchHistory.setBranchId(branch.getId());
		branchHistory.setOrganizationId(branch.getCompany().getOrganization().getId());
		branchHistory.setCompanyId(branch.getCompany().getId());
		branchHistory.setCityId(branch.getCity() != null ? branch.getCity().getId() :null);
		branchHistory.setStateId(branch.getState() != null ? branch.getState().getId() : null);
		branchHistory.setCountryId(branch.getCountry() != null ? branch.getCountry().getId() : null);
		branchHistory.setAddress(branch.getAddress());
		
		saveBranchHistory(Collections.singletonList(branchHistory));
		
	}

	@Override
	public BranchResponseDto getBranch(Long branchId) throws ResourceNotFoundException {
		Branch branch = branchRepository.findById(branchId).filter(b -> !b.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("No branch found"));
		return new BranchResponseDto(branch);
	}

	@Override
	public BranchResponse getAllBranchesByCompay(Long companyId) throws ResourceNotFoundException {
		
		Company company = companyRepository.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("No company found"));
		
		List<Branch> branches = company.getBranches().stream().filter(b -> !b.getDeleted()).collect(Collectors.toList());
		
		return new BranchResponse(branches, company.getOrganization().getId(), company.getId());
	}

	
}
