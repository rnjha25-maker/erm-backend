package ermorg.user_setup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import ermorg.user_setup.dto.request.RoleDTO;
import ermorg.user_setup.dto.response.RoleResponse;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Company;
import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.modal.Role;
import ermorg.user_setup.repository.CompanyRepository;
import ermorg.user_setup.repository.OrganizationRepository;
import ermorg.user_setup.repository.RoleRepository;
import ermorg.user_setup.util.OrganizationContext;

@Service
public class RoleService implements IRoleService {

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private CompanyRepository companyRepository;
	@Autowired
	private OrganizationRepository organizationRepository;

	@Cacheable(value = "role", key = "#roleId")
	@Override
	public RoleResponse getRole(Long roleId) throws ResourceNotFoundException {
		
		Role role = roleRepository.findById(roleId).orElseThrow(()-> new ResourceNotFoundException("Role not foud."));
		return new RoleResponse(role);
	}

	@Override
	public RoleResponse saveRole(RoleDTO request) throws ResourceNotFoundException {
		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.filter(org -> !org.getDeleted())
				.orElseThrow(()->new ResourceNotFoundException("No organization found."));
		Company company = companyRepository.findById(request.getCompanyId()).orElseThrow(()->  new ResourceNotFoundException("Company not found."));
		
		Role role = new Role();
		
//		role.setOrganization(organization);
//		role.setCompany(company);
//		role.setName(request.getRoleName());
		role.setDescription(request.getDescription());
		
		Role savedRole = roleRepository.save(role);
		
		return new RoleResponse(savedRole);
	}
	
	@Caching(evict = {
		    @CacheEvict(value = "roles", key = "#request.companyId"),
		    @CacheEvict(value = "role", key = "#request.roleId")
		})
	@Override
	public RoleResponse updateRole(RoleDTO request) throws ResourceNotFoundException {
		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.filter(org -> !org.getDeleted())
				.orElseThrow(()->new ResourceNotFoundException("No organization found."));
		
		Role role = roleRepository.findById(request.getRoleId()).orElseThrow(()->  new ResourceNotFoundException("Role not found."));
		
//		role.setOrganization(organization);
		role.setDescription(request.getDescription());
		role.setName(request.getRoleName());
		
		Role savedRole = roleRepository.save(role);
		
		return new RoleResponse(savedRole);
	}

//	@Caching(evict = {
//		    @CacheEvict(value = "roles", key = "#companyId"),
//		    @CacheEvict(value = "role", key = "#request.roleId")
//		})
	@Override
	public void deleteRole(Long roleId) throws ResourceNotFoundException {
		
		Role role = roleRepository.findById(roleId).orElseThrow(()-> new ResourceNotFoundException("Role not found."));
		
//		Long companyId = role.getCompany().getId();
		
		role.setDeleted(true);
		roleRepository.save(role);
		
		
	}

	
//	@Cacheable(value = "roles", key = "#companyId")
	@Override
	public List<RoleResponse> getAllRoles(Long companyId) throws ResourceNotFoundException {
		
//		List<Role> roles = roleRepository.findRolesByCompanyId(companyId);
		
//		return roles
//		.stream()
//		.map(role -> new RoleResponse(role))
//		.collect(Collectors.toList());
		
		return null;
	}
	
	

}
