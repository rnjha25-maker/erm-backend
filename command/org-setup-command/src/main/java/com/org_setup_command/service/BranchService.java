package com.org_setup_command.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org_setup_command.dto.request.BranchDTO;
import com.org_setup_command.dto.response.BranchResponse;
import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Address;
import com.org_setup_command.modal.Branch;
import com.org_setup_command.modal.City;
import com.org_setup_command.modal.Company;
import com.org_setup_command.modal.Country;
import com.org_setup_command.modal.Organization;
import com.org_setup_command.modal.State;
import com.org_setup_command.repository.BranchRepository;
import com.org_setup_command.repository.CompanyRepository;
import com.org_setup_command.util.OrganizationContext;

@Service
public class BranchService implements IBranchService {

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ICountryService countryService;
	
	@Autowired
	private CompanyRepository companyRepository;

	@Override
	public BranchResponse createBranch(BranchDTO request) throws ResourceNotFoundException {

		Country country = countryService.getCountry(request.getCountryId());

		List<State> states = country.getStates();

		State state = states.stream().filter(s -> s.getId().equals(request.getStateId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("State not found."));

		City city = state.getCities().stream().filter(c -> c.getId().equals(request.getCityId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("City Not found."));

	
		Company company = companyRepository.findById(request.getCompanyId())
		.filter(cmp -> !cmp.getDeleted())
		.orElseThrow(() -> new ResourceNotFoundException("Company not found."));
		
		

		Branch branch = new Branch();
		branch.setName(request.getBranchName());
		branch.setDescription(request.getBranchDescription());
		branch.setType(request.getType());
		branch.setAddress(request.getAddress());
		branch.setCity(city);
		branch.setState(state);
		branch.setCountry(country);
		branch.setCompany(company);

		Branch savedBranch = branchRepository.save(branch);
		return new BranchResponse(savedBranch);
	}

	@Override
	public BranchResponse updateBranch(BranchDTO request) throws ResourceNotFoundException, InvalidDataException {

		Country country = countryService.getCountry(request.getCountryId());

		Branch branch = branchRepository.findById(request.getBranchId()).filter(br -> !br.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No branch found."));

		List<State> states = country.getStates();

		State state = states.stream().filter(s -> s.getId().equals(request.getStateId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("State not found."));

		City city = state.getCities().stream().filter(c -> c.getId().equals(request.getCityId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("City Not found."));

		
		
		branch.setName(request.getBranchName());
		branch.setDescription(request.getBranchDescription());
		branch.setType(request.getType());
		branch.setAddress(request.getAddress());
		branch.setCity(city);
		branch.setState(state);
		branch.setCountry(country);

		Branch savedBranch = branchRepository.save(branch);
		return new BranchResponse(savedBranch);
	}

	@Override
	public BranchResponse deleteBranch(Long id) throws InvalidDataException, ResourceNotFoundException {
		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No branch found."));

		branch.setDeleted(true);
		branchRepository.save(branch);

		return new BranchResponse(branch);

	}

	@Override
	public BranchResponse getBranch(Long id) throws ResourceNotFoundException {

		Branch branch = branchRepository.findById(id).filter(br -> !br.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No branch found."));

		return new BranchResponse(branch);
	}

	@Override
	public List<BranchResponse> getAllBranch(Long companyId) throws ResourceNotFoundException {
		
		List<Branch> branches = branchRepository.findBranchesByCompayId(companyId);
		
		if(branches == null) throw new ResourceNotFoundException("No branch found.");
		
		return branches
		.stream()
		.map(branch-> new BranchResponse(branch))
		.collect(Collectors.toList());
		
	}

}
