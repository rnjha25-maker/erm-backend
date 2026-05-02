package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.request.UpdateModuleRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.ModuleRightRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.OrgModuleRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.OrganizationDTO;
import ermorg.erm.erm_command_organization.dto.responseDTO.OrganizationResponse;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.City;
import ermorg.erm.erm_command_organization.model.Country;
import ermorg.erm.erm_command_organization.model.ModuleOrganization;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.model.Plan;
import ermorg.erm.erm_command_organization.model.Right;
import ermorg.erm.erm_command_organization.model.Role;
import ermorg.erm.erm_command_organization.model.State;
import ermorg.erm.erm_command_organization.model.User;
import ermorg.erm.erm_command_organization.model.UserDetail;
import ermorg.erm.erm_command_organization.model.history.ModuleOrganizationHistory;
import ermorg.erm.erm_command_organization.model.history.OrganizationHistory;
import ermorg.erm.erm_command_organization.model.history.PlanHistory;
import ermorg.erm.erm_command_organization.model.history.RightOrganizationHistory;
import ermorg.erm.erm_command_organization.repository.CityRepository;
import ermorg.erm.erm_command_organization.repository.CountryRepository;
import ermorg.erm.erm_command_organization.repository.ModuleOrganizationRepository;
import ermorg.erm.erm_command_organization.repository.ModuleRepository;
import ermorg.erm.erm_command_organization.repository.OrganizationHistoryRepository;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;
import ermorg.erm.erm_command_organization.repository.PlanRepository;
import ermorg.erm.erm_command_organization.repository.RightRepository;
import ermorg.erm.erm_command_organization.repository.RoleRepository;
import ermorg.erm.erm_command_organization.repository.StateRepository;
import ermorg.erm.erm_command_organization.repository.UserDetailRepository;
import ermorg.erm.erm_command_organization.repository.UserRepository;
import ermorg.erm.erm_command_organization.repository.history.RightOrganizationHistoryRepository;
import ermorg.erm.erm_command_organization.service.IOrganizationService;
import ermorg.erm.erm_command_organization.util.AuditorAwareImpl;
import ermorg.erm.erm_command_organization.util.PasswordGenerator;

import jakarta.transaction.Transactional;

@Service
public class OrganizationService implements IOrganizationService {
	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDetailRepository UserDetailRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuditorAware auditor;

	@Autowired
	private OrganizationHistoryRepository organizationHistoryRepository;

	@Autowired
	private UserDetailRepository userDetailsRepository;

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private ModuleOrganizationRepository moduleOrganizationRepository;
	
	@Autowired
	private OrganizationHistoryRepository oganizationHistoryRepository;
	
	@Autowired
	private RightRepository rightRepository;
	
	@Autowired
	private RightOrganizationHistoryRepository rightOrganizationHistoryRepository;
	@Override
	public OrganizationResponse createOrganization(OrganizationDTO request) throws ResourceNotFoundException {
		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

		Country country = countryRepository.findById(request.getCountryId())
				.orElseThrow(() -> new ResourceNotFoundException("No country found."));

		State state = country.getStates().stream().filter(state1 -> state1.getId().equals(request.getStateId()))
				.findAny().orElseThrow(() -> new ResourceNotFoundException("No state found."));

		City city = state.getCities().stream().filter(city1 -> city1.getId().equals(request.getCityId())).findAny()
				.orElseThrow(() -> new ResourceNotFoundException("No city found."));

		Plan plan = planRepository.findById(request.getPlanId()).orElseThrow(() -> new ResourceNotFoundException("No plan found."));
		
		Organization organization = new Organization();
		organization.setState(state);
		organization.setCity(city);
		organization.setCountry(country);
		organization.setName(request.getName());
		organization.setOrganizationLogoImageUrl(request.getOrgImgUrl());
		organization.setClientIP(clientIp);
		organization = organizationRepository.save(organization);
		organization.setDeleted(false);
		organization.setClientIP(clientIp);
		organization.setPinCode(request.getPinCode());
		organization.setCompanyCount(request.getCompanyCount());
		organization.setPlan(plan);
		organization.setGstNo(request.getGstNo());
		organization.setPanNo(request.getPanNo());
		organization.setNoOfBasicUsers(request.getNoOfBasicUsers());
		organization.setNoOfAdvancedUsers(request.getNoOfAdvancedUsers());
		organization.setBusinessLocation(request.getBusinessLocation());
		organization.setAdminCount(request.getAdminCount());
//		role.setOrganization(organization);
		 Role role = roleRepository.findByRoleName("orgAdmin");
		 
		 if(role == null) throw new ResourceNotFoundException("Please create orgAdmin role first.");
//		role.setName("orgAdmin");
//		role.setDescription("OA");
//		role.setClientIP(clientIp);
		role = roleRepository.save(role);

		UserDetail userDetail = new UserDetail();
		userDetail.setFirstName(request.getAdminFirstName());
		userDetail.setMiddleName(request.getAdminMiddleName());
		userDetail.setLastName(request.getAdminLastName());
		userDetail.setPhone(request.getPhone());
		userDetail.setAlternatePhone(request.getAlternatePhone());
		userDetail.setOrganization(organization);
		userDetail.setClientIP(clientIp);
		userDetail = UserDetailRepository.save(userDetail);
		userDetail.setEmail(request.getEmail());
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(PasswordGenerator.generate(8));
		user.setUserDetail(userDetail);
		ArrayList<Role> roleList = new ArrayList<>();
		roleList.add(role);
		user.setRoles(roleList);
		user.setClientIP(clientIp);
		user.setOrganization(organization);
		User savedUser = userRepository.save(user);
		if(request.isAdminPoc()) {
			organization.setPOCPerson(savedUser.getUserDetail());
			
			 organization = organizationRepository.save(organization);
			 organizationRepository.flush();
		}
		

		return new OrganizationResponse(organization);
	}

	@Transactional
	@Override
	public OrganizationResponse updateOrganization(OrganizationDTO request) throws DataNotFoundException {
		String clientIp = ((AuditorAwareImpl) auditor).getClientIp();
		Optional<Organization> organizationOptional = organizationRepository.findById(request.getOrganizationId());
		if (organizationOptional.isEmpty()) {
			throw new DataNotFoundException("Organization not found!");
		}
		Plan plan = planRepository.findById(request.getPlanId()).orElseThrow(() -> new DataNotFoundException("Plan not found."));
		Organization organization = organizationOptional.get();
		organization.setName(request.getName());
		organization.setOrganizationLogoImageUrl(request.getOrgImgUrl());
		organization.setClientIP(clientIp);
		organization.setPlan(plan);
		organization.setCompanyCount(request.getCompanyCount());
		organization.setStatus(request.getStatus());
//		organization.setPanNo(request.getPanNo());
//		organization.setGstNo(request.getGstNo());
//		organization.setNoOfBasicUsers(request.getNoOfBasicUsers());
//		organization.setNoOfAdvancedUsers(request.getNoOfAdvancedUsers());
		Organization savedOrganization = organizationRepository.save(organization);

		saveOrganizationHistory(savedOrganization, "U", false, null);
		return new OrganizationResponse(savedOrganization);
	}

	@Transactional
	@Override
	public void deleteOrganization(Long id) throws InvalidDataException {
		Organization organization = organizationRepository.findById(id).filter(o -> !o.getDeleted())
				.orElseThrow(() -> new DataNotFoundException("No organization found."));

		organization.setDeleted(true);

		organizationRepository.save(organization);
		saveOrganizationHistory(organization, "D", true, "all");
	}

	public void saveOrganizationHistory(Organization organization, String operation, boolean moduleChange, String moduleType) {
		OrganizationHistory org = new OrganizationHistory();

		org.setOrganizationId(organization.getId());
		org.setClientIP(organization.getClientIP());
		org.setCreatedAt(organization.getCreatedAt());
		org.setCreatedBy(organization.getCreatedBy());
		org.setOperation(operation);
		org.setName(organization.getName());
		org.setOrganizationLogoImageUrl(organization.getOrganizationLogoImageUrl());

		PlanHistory planHistory = new PlanHistory();
		Plan plan = organization.getPlan();
		
		planHistory.setPlanDescription(plan == null ? null : plan.getPlanDescription());
		planHistory.setPlanName(plan == null ? null :plan.getPlanName());
		planHistory.setPlanId(plan == null ? null :plan.getId());
		planHistory.setOperation(operation);
		org.setPlan(planHistory);
		
		if(moduleChange) {
			List<ModuleOrganization> modules = organization.getModules();
			for(ModuleOrganization module: modules) {
				if(!"all".equals(moduleType) || ("view".equals(moduleType) && module.getViewFieldId() == null) || ("category".equals(moduleType) && module.getFieldId() == null)) {
					continue;
				}
				ModuleOrganizationHistory moduleOrganizationHistory = new ModuleOrganizationHistory();
				moduleOrganizationHistory.setCategoryId(module.getCategoryId());
				moduleOrganizationHistory.setFieldId(module.getFieldId());
				moduleOrganizationHistory.setViewCategoryId(module.getViewCategoryId());
				moduleOrganizationHistory.setViewFieldId(module.getViewFieldId());
				moduleOrganizationHistory.setModuleId(module.getModuleId());
				org.getModules().add(moduleOrganizationHistory);
			}
		
		}
		
		organizationHistoryRepository.save(org);

	}

	@Override
	public List<OrganizationResponse> getAllOrganization() {

		List<Organization> organizations = organizationRepository.findAll();

		return organizations.stream().filter(org -> !org.getDeleted())
				.map(organization -> new OrganizationResponse(organization)).collect(Collectors.toList());
	}

	@Override
	public OrganizationResponse getOrganization(Long organizationId, int back) throws DataNotFoundException {
		
		OrganizationHistory organizationHistory = null;
		if(back >0) {
//			EntityManager entityManager = Persistence.createEntityManagerFactory("erm-command-organization-PU").createEntityManager();
//
//			String query = "SELECT e FROM OrganizationHistory e ORDER BY e.createdAt DESC";
//			Query queryObject = entityManager.createQuery(query);
//			queryObject.setMaxResults(1);
//			 organizationHistory = (OrganizationHistory) queryObject.getSingleResult();
//			 
			 organizationHistory = organizationHistoryRepository.findLastModified(organizationId);
//			entityManager.close();
		}
			
		Organization organization = organizationRepository.findById(organizationId).filter(org -> !org.getDeleted())
				.orElseThrow(() -> new DataNotFoundException("No organization found."));
		;

		return new OrganizationResponse(organization, organizationHistory);
	}

	@Override
	public List<Map<String, Object>> getAllModules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public UpdateModuleRequest updateModule(UpdateModuleRequest request) {

		Organization organization = organizationRepository.findById(request.getOrgId())
				.orElseThrow(() -> new RuntimeException("No organization found"));

		List<ModuleOrganization> existingModules = organization.getModules();

		long existingModulesCount = existingModules.stream().filter(module -> module.getFieldId() != null).count();
		boolean isCategoryDeleted = existingModulesCount != 0;
		// Remove existing module organizations
		existingModules.forEach(module -> moduleOrganizationRepository.delete(module));
		existingModules.clear();
		organization.setModules(existingModules);

		List<OrgModuleRequest> orgModules = request.getOrgModules();
//		List<ModuleOrganization> newModules = orgModules.stream().map(orgModule -> {
//			ModuleOrganization moduleOrganization = new ModuleOrganization();
//			orgModule.getCategories().stream().forEach(category -> {
//				category.getFieldIds().stream().forEach(field -> {
//					moduleOrganization.setModuleId(orgModule.getModuleId());
//					moduleOrganization.setCategoryId(category.getCategoryId());
//					moduleOrganization.setFieldId(field);
//					moduleOrganization.setOrganization(organization);
//				});
//
//			});
//			return moduleOrganization;
//		}).collect(Collectors.toList());
		
		List<ModuleOrganization> newModules = orgModules.stream()
		        .flatMap(orgModule -> orgModule.getCategories().stream()
		                .flatMap(category -> category.getFieldIds().stream()
		                        .map(field -> {
		                            ModuleOrganization moduleOrganization = new ModuleOrganization();
		                            moduleOrganization.setModuleId(orgModule.getModuleId());
		                            moduleOrganization.setCategoryId(category.getCategoryId());
		                            moduleOrganization.setFieldId(field);
		                            moduleOrganization.setOrganization(organization);
		                            return moduleOrganization;
		                        })))
		        .collect(Collectors.toList());

		organization.getModules().addAll(newModules);

		organizationRepository.save(organization);

		if (isCategoryDeleted) {
			saveOrganizationHistory(organization, "U", true, "category");
		}
		return request;
	}

	@Override
	@Transactional
	public UpdateModuleRequest updateModuleView(UpdateModuleRequest request) throws ResourceNotFoundException {


		Organization organization = organizationRepository.findById(request.getOrgId())
				.orElseThrow(() -> new RuntimeException("No organization found"));

		
		List<ModuleOrganization> existingModules = organization.getModules();

		long existingModulesCount = existingModules.stream().filter(module -> module.getViewFieldId() != null).count();
		boolean isViewDeleted = existingModulesCount != 0;
		// Remove existing module organizations
		existingModules.forEach(module -> moduleOrganizationRepository.delete(module));

		existingModules.clear();
		organization.setModules(existingModules);

		List<OrgModuleRequest> orgModules = request.getOrgModules();
		List<ModuleOrganization> newModules = orgModules.stream().map(orgModule -> {
			ModuleOrganization moduleOrganization = new ModuleOrganization();
			orgModule.getCategories().stream().forEach(category -> {
				category.getFieldIds().stream().forEach(field -> {
					moduleOrganization.setModuleId(orgModule.getModuleId());
					moduleOrganization.setViewCategoryId(category.getCategoryId());
					moduleOrganization.setViewFieldId(field);
					moduleOrganization.setOrganization(organization);
				});

			});
			return moduleOrganization;
		}).collect(Collectors.toList());

		organization.getModules().addAll(newModules);

		organizationRepository.save(organization);
		
		if(isViewDeleted) {
			saveOrganizationHistory(organization, "U", true, "view");
		}
		return request;
	
	}

	@Override
	public ModuleRightRequest updateRight(ModuleRightRequest request) throws ResourceNotFoundException {
		
		Organization organization = organizationRepository.findById(request.getOrganizationId()).filter(org -> !org.getDeleted())
				.orElseThrow(() -> new DataNotFoundException("No organization found."));
		
		List<Right> orgRights = organization.getRights();
		
		List<Long> rightIds = request.getModuleRights().stream().map(moduleRight -> {
			return moduleRight.getRightIds();
		})
		.flatMap(List::stream)
		.collect(Collectors.toList());
		
		List<Right> oldRights = orgRights;

		List<Right> foundRights = rightRepository.findAllByRightIds(rightIds);
		orgRights = foundRights;
		organization.setRights(orgRights);
		 boolean removed = oldRights.removeAll(foundRights);
		organizationRepository.save(organization);
		
		if(removed) {
			saveRightOrganizationHistory(oldRights, organization.getId(), "U");

		}
		
		return request;
	}
	
	private void saveRightOrganizationHistory(List<Right> rights, Long organizationId,  String operation) {
		
		List<RightOrganizationHistory> rightOrganizationHistoryList = rights.stream().map(right->{
			RightOrganizationHistory rightOrganizationHistory = new RightOrganizationHistory();
			rightOrganizationHistory.setOrganizationId(organizationId);
			rightOrganizationHistory.setRightId(right.getId());
			rightOrganizationHistory.setOperation(operation);
			return rightOrganizationHistory;
		})
		.collect(Collectors.toList());
		
		rightOrganizationHistoryRepository.saveAll(rightOrganizationHistoryList);
	}
	
	

}
