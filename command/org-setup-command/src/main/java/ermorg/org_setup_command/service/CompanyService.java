package ermorg.org_setup_command.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.org_setup_command.dto.request.CompanyDTO;
import ermorg.org_setup_command.dto.response.BranchResponse;
import ermorg.org_setup_command.dto.response.CompanyResponse;
import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.modal.Company;
import ermorg.org_setup_command.modal.Organization;
import ermorg.org_setup_command.modal.Right;
import ermorg.org_setup_command.modal.Role;
import ermorg.org_setup_command.modal.RoleRight;
import ermorg.org_setup_command.modal.history.CompanyHistory;
import ermorg.org_setup_command.repository.CompanyHistoryRepository;
import ermorg.org_setup_command.repository.CompanyRepository;
import ermorg.org_setup_command.repository.OrganizationRepository;
import ermorg.org_setup_command.repository.RightRepository;
import ermorg.org_setup_command.repository.RoleRepository;
import ermorg.org_setup_command.repository.RoleRightRepository;

import jakarta.transaction.Transactional;

@Service
public class CompanyService implements ICompanyService {

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private IOrganizationService organizationService;

	@Autowired
	private CompanyHistoryRepository companyHistoryRepository;

	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private RoleRightRepository roleRightRepository;
	
	@Autowired
	private RightRepository rightRepository;
	@Transactional
	@Override
	public CompanyResponse createCompany(CompanyDTO request) throws ResourceNotFoundException {
	
		Organization organization = organizationRepository.findById(request.getOrganizationId())
		.filter(org -> !org.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("Organization not found."));
		
		Company company = new Company();
		company.setOrganization(organization);
		company.setCompanyLogoImageUrl(request.getLogo());
		company.setName(request.getCompanyName());

		Company savedCompany = companyRepository.save(company);
		
//		Role orgRole = new Role();
		
//		List<Role> orgRoles = roleRepository.getRolesByOrganizationId(organization.getId());
		
		
//		List<Right> rights = rightRepository.findAll();
		
//		List<Right> filterdRights = rights.stream()
//		.filter(r -> r.getName().equals("user-setup") || r.getName().equals("org-setup"))
//		.collect(Collectors.toList());
//		
//		
//		if(orgRoles == null || orgRoles.size() == 0) {
//			
//
//			
//			orgRole.setName("ORG_ADMIN");
//			orgRole.setCompany(savedCompany);
//			orgRole.setOrganization(organization);
//			
//			orgRole.setDescription("Organization Admin Role");
//			
//			
//			Role createdRole = createRole(orgRole);
//			
//			Set<RoleRight> roleRights = new HashSet<>();
//			filterdRights.stream()
//			.forEach(right->{
//				RoleRight roleRight = new RoleRight();
//				
//				roleRight.setRight(right);
//				roleRight.setRole(createdRole);
//				roleRight.setOrganization(organization);
//				
//				RoleRight savedRoleRight = roleRightRepository.save(roleRight);
//				roleRights.add(savedRoleRight);
//			});
//			
//			createdRole.setRoleRights(roleRights);
//			
//			roleRepository.save(createdRole);
//			
//		}
		
//		List<Role> cmpRoles = roleRepository.getRolesByCompanyId(company.getId());
//		
//		if(cmpRoles == null || cmpRoles.size() <=1) {
//			orgRole.setName("CMP_ADMIN");
//			orgRole.setCompany(savedCompany);
//			orgRole.setOrganization(organization);
//			
//			orgRole.setDescription("Organization Admin Role");
//			Role createdRole = createRole(orgRole);
//			Set<RoleRight> roleRights = new HashSet<>();
//			filterdRights.stream()
//			.forEach(right->{
//				RoleRight roleRight = new RoleRight();
//				
//				roleRight.setRight(right);
//				roleRight.setRole(createdRole);
//				roleRight.setOrganization(organization);
//				
//				RoleRight savedRoleRight = roleRightRepository.save(roleRight);
//				roleRights.add(savedRoleRight);
//			});
//			
//			createdRole.setRoleRights(roleRights);
//			roleRepository.save(createdRole);
//			
//		}
		
		return  createCompanyResponse(savedCompany);
	}

	@Transactional
	private Role createRole (Role role) {
		
		return roleRepository.save(role);
		
	}
	@Transactional
	@Override
	public CompanyResponse updateCompany(CompanyDTO request) throws ResourceNotFoundException {
		
		Company company = companyRepository.findById(request.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("No company found"));
		company.setName(request.getCompanyName());
		company.setCompanyLogoImageUrl(request.getLogo());
		
		Company savedCompany = companyRepository.save(company);
		saveCompanyHistory(savedCompany, "U");
		
		return createCompanyResponse(savedCompany);

	}

	@Transactional
	@Override
	public CompanyResponse deleteCompany(Long id) throws ResourceNotFoundException {
		Company company = companyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No company found"));

		company.setDeleted(true);
		companyRepository.save(company);
		saveCompanyHistory(company, "D");
		
		return createCompanyResponse(company);

	}

	private void saveCompanyHistory(Company company, String Operation) {
		CompanyHistory cmp = new CompanyHistory();
		cmp.setOperation(Operation);
		cmp.setCompanyId(company.getId());
		cmp.setCompanyLogoImageUrl(company.getCompanyLogoImageUrl());
		cmp.setName(company.getName());
		cmp.setOrganizationId(company.getOrganization().getId());
		
		companyHistoryRepository.save(cmp);
	}
	
	private CompanyResponse createCompanyResponse(Company company) {
		
		List<BranchResponse> branches = company.getBranches()
		.stream()
		.map(branch-> new BranchResponse(branch))
		.collect(Collectors.toList());
		
		return new CompanyResponse(company, branches);
		
	}

	@Override
	public CompanyResponse getCompany(Long companyId) throws ResourceNotFoundException {
		
		Company company = companyRepository.findById(companyId)
		.filter(cmp -> !cmp.getDeleted())
		.orElseThrow(() -> new ResourceNotFoundException("No company found"));
		List<BranchResponse> branches = company.getBranches()
		.stream()
		.map(branch -> new BranchResponse(branch))
		.collect(Collectors.toList());
		return new CompanyResponse(company, branches);
	}

	@Override
	public List<CompanyResponse> getAllCompany(Long organizationId) throws ResourceNotFoundException {
		
		List<Company> companies = companyRepository.findAllCompaniesByOrganizationId(organizationId);
		
		return companies.stream()
		.map(cmp ->{
			List<BranchResponse> branches = cmp.getBranches()
			.stream().map(branch -> new BranchResponse(branch))
			.collect(Collectors.toList());
			
			return new CompanyResponse(cmp, branches);
		})
		.collect(Collectors.toList());
		
	}

	
}
