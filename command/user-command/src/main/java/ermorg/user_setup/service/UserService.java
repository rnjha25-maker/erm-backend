package ermorg.user_setup.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.user_setup.constant.ErmStakeholderRole;
import ermorg.user_setup.dto.request.UserDTO;
import ermorg.user_setup.dto.response.RightResponse;
import ermorg.user_setup.dto.response.RoleResponse;
import ermorg.user_setup.dto.response.UserResponse;
import ermorg.user_setup.exception.InvalidResourceAccess;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.modal.Address;
import ermorg.user_setup.modal.Branch;
import ermorg.user_setup.modal.City;
import ermorg.user_setup.modal.Company;
import ermorg.user_setup.modal.Country;
import ermorg.user_setup.modal.Department;
import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.modal.Role;
import ermorg.user_setup.modal.State;
import ermorg.user_setup.modal.User;
import ermorg.user_setup.modal.UserDetail;
import ermorg.user_setup.repository.CountryRepository;
import ermorg.user_setup.repository.OrganizationRepository;
import ermorg.user_setup.repository.RoleRepository;
import ermorg.user_setup.repository.UserDetailRepository;
import ermorg.user_setup.repository.UserRepository;
import ermorg.user_setup.util.OrganizationContext;
import ermorg.user_setup.util.PasswordGenerator;

@Service
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ICityService cityService;

	@Autowired
	private IStateService stateService;
	
	@Autowired
	private CountryRepository countryRepository;
	
	@Autowired
	private UserDetailRepository userDetailsRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;
	@Transactional
	@Override
	public UserResponse saveUser(UserDTO request) throws ResourceNotFoundException, InvalidResourceAccess {
		
		Organization organization = organizationRepository.findByIdAndDeletedFalse(request.getOrganizationId())
		.orElseThrow(()->new ResourceNotFoundException("No organization found."));
		
		Long count = userDetailsRepository.countByEmail(request.getEmail());
		
		if(request.getUserId() <= 0 && count !=null && count > 0) {
			throw new RuntimeException("Email already exist.");
		}
		UserDetail userDetails = new UserDetail();
		
		userDetails.setFirstName(request.getFirstName());
		userDetails.setLastName(request.getLastName());
		
		Country country = countryRepository.findById(request.getCountryId())
				.orElseThrow(()->new ResourceNotFoundException("No country found."));
		
		City city = cityService.getCity(request.getCityId());
		
		State state = stateService.getState(request.getStateId());
		
		Address address = new Address();
		
		address.setCity(city);
		address.setState(state);
		address.setCountry(country);
		address.setAddress(request.getAddress());
		
		Company compony = organization.getCompanies()
		.stream()
		.filter(cmp->cmp.getId().equals(request.getComponyId()))
		.findFirst()
		.orElseThrow(()-> new ResourceNotFoundException("Compony not found."));
		
		Branch branch = compony.getBranches()
		.stream()
		.filter(br-> br.getId().equals(request.getBranchId()))
		.findAny()
		.orElseThrow(()-> new ResourceNotFoundException("Branch not found."));
		
		
		Department department = branch.getDepartments()
		.stream()
		.filter(d -> d.getId().equals(request.getDepartmentId()))
		.findAny()
		.orElseThrow(()-> new ResourceNotFoundException("Department not found."));
		
		List<Role> roles = StreamSupport.stream(roleRepository.findAllById(request.getRoleIds()).spliterator(), false)
                .collect(Collectors.toList());
		if (roles.isEmpty()) {
		    throw new ResourceNotFoundException("Role(s) not found.");
		}
		
		userDetails.setOrganization(organization);
		userDetails.setBranch(branch);
		userDetails.setDepartment(department);
		userDetails.setPhone(request.getPhone());
		userDetails.setEmail(request.getEmail());
		UserDetail savedUserDetails = userDetailsRepository.save(userDetails);
		
		User user = new User();
		
		user.setUserDetail(savedUserDetails);
		user.setEmail(request.getEmail());
		user.setPassword(PasswordGenerator.generate(8));
		user.setClientIP(userDetails.getClientIP());
		user.setRoles(roles);
		user.setCompany(compony);
		user.setBranch(branch);
		user.setDepartment(department);
		
		User savedUser = userRepository.save(user);
		
		return mapUserResponse(savedUser);
		
		
	}

	
	
//	@Caching(evict= {
//			@CacheEvict(value="user", key="#request.userId"),
//			@CacheEvict(value="users", key="request.companyId")
//	})
	@Override
	public UserResponse updateUser(UserDTO request) 
	        throws ResourceNotFoundException, InvalidResourceAccess {

	    User user = getUserOrThrow(request.getUserId());

	    Organization organization = getOrganizationOrThrow(request.getOrganizationId());

	    UserDetail userDetails = user.getUserDetail();

	    updateBasicUserDetails(userDetails, request);

	    Company company = getCompanyOrThrow(organization, request.getComponyId());
	    Branch branch = getBranchOrThrow(company, request.getBranchId());
	    Department department = getDepartmentOrThrow(branch, request.getDepartmentId());

	    List<Role> roles = getRolesOrThrow(request.getRoleIds());

	    // Update user details
	    userDetails.setOrganization(organization);
	    userDetails.setBranch(branch);
	    userDetails.setDepartment(department);
	    userDetails.setPhone(request.getPhone());

	    UserDetail savedUserDetails = userDetailsRepository.save(userDetails);

	    // Update user
	    user.setUserDetail(savedUserDetails);
	    user.setEmail(request.getEmail());
	    user.setClientIP(userDetails.getClientIP());
	    user.setRoles(roles);
	    user.setBranch(branch);
	    user.setDepartment(department);

	    return mapUserResponse(userRepository.save(user));
	}	
	private User getUserOrThrow(Long userId) throws ResourceNotFoundException {
	    return userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
	}
	
	private Organization getOrganizationOrThrow(Long orgId) throws ResourceNotFoundException {
	    return organizationRepository.findByIdAndDeletedFalse(orgId)
	            .orElseThrow(() -> new ResourceNotFoundException("No organization found."));
	}
	
	private Company getCompanyOrThrow(Organization org, Long companyId) throws ResourceNotFoundException {
	    return org.getCompanies().stream()
	            .filter(c -> c.getId().equals(companyId))
	            .findFirst()
	            .orElseThrow(() -> new ResourceNotFoundException("Company not found."));
	}
	
	private Branch getBranchOrThrow(Company company, Long branchId) throws ResourceNotFoundException {
	    return company.getBranches().stream()
	            .filter(b -> b.getId().equals(branchId))
	            .findFirst()
	            .orElseThrow(() -> new ResourceNotFoundException("Branch not found."));
	}
	
	private List<Role> getRolesOrThrow(List<Long> roleIds) throws ResourceNotFoundException {
	    List<Role> roles = StreamSupport
	            .stream(roleRepository.findAllById(roleIds).spliterator(), false)
	            .collect(Collectors.toList());

	    if (roles.isEmpty()) {
	        throw new ResourceNotFoundException("Role(s) not found.");
	    }

	    return roles;
	}
	
	private Department getDepartmentOrThrow(Branch branch, Long deptId) throws ResourceNotFoundException {
	    return branch.getDepartments().stream()
	            .filter(d -> d.getId().equals(deptId))
	            .findFirst()
	            .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
	}
	
	private void updateBasicUserDetails(UserDetail userDetails, UserDTO request) 
	        throws ResourceNotFoundException {

	    userDetails.setFirstName(request.getFirstName());
	    userDetails.setLastName(request.getLastName());

	    Country country = countryRepository.findById(request.getCountryId())
	            .orElseThrow(() -> new ResourceNotFoundException("No country found."));

	    City city = cityService.getCity(request.getCityId());
	    State state = stateService.getState(request.getStateId());

//	    Address address = new Address();
//	    address.setCity(city);
//	    address.setState(state);
//	    address.setCountry(country);
//	    address.setAddress(request.getAddress());
//
//	    userDetails.setAddress(address); // IMPORTANT (was missing)
	}
	
//	@Cacheable(value="user", key="#userId")
	@Override
	public UserResponse getUser(Long userId) throws ResourceNotFoundException, InvalidResourceAccess {
		
				
		User user = userRepository.findById(userId)
				 .filter(u-> !u.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("User not found."));
		
		
		return mapUserResponse(user);
	}

	

//	@Cacheable(value="users", key="#companyId")
	@Override
	public List<UserResponse> getAllUser(Long companyId) throws ResourceNotFoundException, InvalidResourceAccess {

		List<User> users = userRepository.findAllUsersByCompanyId(companyId);

		return users.stream().map(this::mapUserWithRoleResponses).collect(Collectors.toList());

	}

	@Transactional(readOnly = true)
	@Override
	public List<UserResponse> getUsersByRole(Long companyId, ErmStakeholderRole role)
			throws ResourceNotFoundException, InvalidResourceAccess {

		List<User> users = userRepository.findByCompanyIdAndRoleNameIgnoreCase(companyId,
				role.getDisplayName());

		return users.stream().map(this::mapUserWithRoleResponses).collect(Collectors.toList());
	}

	private UserResponse mapUserWithRoleResponses(User user) {
		List<RoleResponse> roles = user.getRoles().stream().map(role -> {
			List<RightResponse> rights = role.getRoleRights().stream().map(RightResponse::new)
					.collect(Collectors.toList());
			return new RoleResponse(role, rights);
		}).collect(Collectors.toList());
		return new UserResponse(user, roles);
	}

	private UserResponse mapUserResponse(User user) {
		
		List<RoleResponse> roles = user.getRoles().stream()
		        .map(role -> new RoleResponse(role, role.getRoleRights().stream()
		                .map(rt -> new RightResponse(rt))
		                .collect(Collectors.toList())))
		        .collect(Collectors.toList());
		
		return new UserResponse(user, roles);
	}

}
