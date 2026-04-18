package ermorg.user_setup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ermorg.user_setup.dto.request.RoleRightDTO;
import ermorg.user_setup.dto.response.RightResponse;
import ermorg.user_setup.dto.response.RoleResponse;
import ermorg.user_setup.exception.InvalidResourceAccess;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.modal.Right;
import ermorg.user_setup.modal.Role;
import ermorg.user_setup.modal.RoleRight;
import ermorg.user_setup.repository.CompanyRepository;
import ermorg.user_setup.repository.OrganizationRepository;
import ermorg.user_setup.repository.RightRepository;
import ermorg.user_setup.repository.RoleRightRepository;

import jakarta.transaction.Transactional;

@Service
public class RightsService implements IRightService {

	@Autowired
	private RightRepository rightRepository;
	@Autowired
	private RoleRightRepository roleRightRepository;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	
	@Cacheable(value="rights", key="organizationId")
	@Override
	public List<RightResponse> getAllRights(Long organizationId) throws ResourceNotFoundException, InvalidResourceAccess {
		
		Organization organization = organizationRepository.findById(organizationId)
				.filter(org -> !org.getDeleted())
				.orElseThrow(()->new ResourceNotFoundException("No organization found."));
		
		if(organization == null) throw new InvalidResourceAccess("Invalid resource access.");
		
//		List<RightResponse> rights = organization.getRoleRight()
//		.stream()
//		.map(rr-> {
//		 	return new RightResponse(rr);
//		})
//		.collect(Collectors.toList());
		
//		return rights;
		return null;
	}
	
	
	
	@Transactional
	@Override
	public RoleResponse mapRoleRight(RoleRightDTO request) throws ResourceNotFoundException, InvalidResourceAccess {
		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.filter(org -> !org.getDeleted())
				.orElseThrow(()->new ResourceNotFoundException("No organization found."));
		
//		List<RoleRight> roleRights = organization.getRoleRight();
//		Role role = roleRights
//		.stream()
//		.map(rr-> rr.getRole())
//		.filter(r-> r.getId().equals(request.getRightId()))
//		.findAny()
//		.orElseThrow(()-> new ResourceNotFoundException("Role not found."));
//		
//		Right right = organization.getRoleRight()
//		.stream()
//		.map(rr-> rr.getRight())
//		.filter(r-> r.getId().equals(request.getRightId()))
//		.findAny()
//		.orElseThrow(()-> new ResourceNotFoundException("No right found"));
//		
//		RoleRight roleRight = new RoleRight();
//		
//		roleRight.setRight(right);
//		roleRight.setRole(role);
//		roleRight.setOrganization(organization);
		
//		RoleRight savedRoleRight = roleRightRepository.save(roleRight);
//		roleRights.add(savedRoleRight);
//		
//		List<RightResponse> rights = roleRights.stream()
//		.map(r-> new RightResponse(r))
//		.collect(Collectors.toList());
//		
//		return new RoleResponse(role, rights);
		return null;
	
	}
	
	

}
