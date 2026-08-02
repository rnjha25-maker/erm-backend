package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.request.UpdateModuleRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.ModuleRightRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.OrgCategoryRequest;
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
	private UserDetailRepository userDetailRepository; // ✅ removed duplicate userDetailsRepository
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private AuditorAware auditor;
	@Autowired
	private OrganizationHistoryRepository organizationHistoryRepository; // ✅ removed duplicate
																			// oganizationHistoryRepository
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
	private RightRepository rightRepository;
	@Autowired
	private RightOrganizationHistoryRepository rightOrganizationHistoryRepository;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationDTO request) throws ResourceNotFoundException {
    	
    	 if (userRepository.existsByEmail(request.getEmail()) 
    	            || userDetailRepository.existsByEmail(request.getEmail())) {
    	        throw new InvalidDataException("Email already exists!");
    	    }
    	 
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("No country found."));

        State state = country.getStates().stream()
                .filter(s -> s.getId().equals(request.getStateId()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No state found."));

        City city = state.getCities().stream()
                .filter(c -> c.getId().equals(request.getCityId()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No city found."));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("No plan found."));

        Role role = roleRepository.findByRoleName("orgAdmin");
        if (role == null)
            throw new ResourceNotFoundException("Please create orgAdmin role first.");

        // ✅ Set all fields before saving — removed redundant first save
        Organization organization = new Organization();
        organization.setCountry(country);
        organization.setState(state);
        organization.setCity(city);
        organization.setPlan(plan);
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());   // ✅ was missing
        organization.setOrganizationLogoImageUrl(request.getOrgImgUrl());
        organization.setPinCode(request.getPinCode());
        organization.setBusinessLocation(request.getBusinessLocation());
        organization.setAdminCount(request.getAdminCount());
        organization.setCompanyCount(request.getCompanyCount());
        organization.setGstNo(request.getGstNo());
        organization.setPanNo(request.getPanNo());
        organization.setNoOfBasicUsers(request.getNoOfBasicUsers());
        organization.setNoOfAdvancedUsers(request.getNoOfAdvancedUsers());
        organization.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE"); // ✅ was missing
        organization.setDeleted(false);
        organization.setClientIP(clientIp);
        organization = organizationRepository.save(organization);

        // ✅ Set email BEFORE saving userDetail
        UserDetail userDetail = new UserDetail();
        userDetail.setFirstName(request.getAdminFirstName());
        userDetail.setMiddleName(request.getAdminMiddleName());
        userDetail.setLastName(request.getAdminLastName());
        userDetail.setEmail(request.getEmail());                  // ✅ moved before save
        userDetail.setPhone(request.getPhone());
        userDetail.setAlternatePhone(request.getAlternatePhone());
        userDetail.setOrganization(organization);
        userDetail.setClientIP(clientIp);
        userDetail = userDetailRepository.save(userDetail);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(PasswordGenerator.generate(8));
        user.setUserDetail(userDetail);
        user.setRoles(List.of(role));                             // ✅ cleaner than new ArrayList + add
        user.setClientIP(clientIp);
        user.setOrganization(organization);
        User savedUser = userRepository.save(user);

        if (request.isAdminPoc()) {
            organization.setPOCPerson(savedUser.getUserDetail());
            organization = organizationRepository.save(organization);
        }

        return new OrganizationResponse(organization);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(OrganizationDTO request) throws DataNotFoundException {
        String clientIp = ((AuditorAwareImpl) auditor).getClientIp();

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new DataNotFoundException("Organization not found!"));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new DataNotFoundException("Plan not found."));

        organization.setName(request.getName());
        organization.setOrganizationLogoImageUrl(request.getOrgImgUrl());
        organization.setClientIP(clientIp);
        organization.setPlan(plan);
        organization.setCompanyCount(request.getCompanyCount());
        organization.setStatus(request.getStatus());
        organization.setPanNo(request.getPanNo());               // ✅ uncommented
        organization.setGstNo(request.getGstNo());               // ✅ uncommented
        organization.setNoOfBasicUsers(request.getNoOfBasicUsers());   // ✅ uncommented
        organization.setNoOfAdvancedUsers(request.getNoOfAdvancedUsers()); // ✅ uncommented

        Organization savedOrganization = organizationRepository.save(organization);
        saveOrganizationHistory(savedOrganization, "U", false, null);
        return new OrganizationResponse(savedOrganization);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id) throws InvalidDataException {
        Organization organization = organizationRepository.findById(id)
                .filter(o -> !o.getDeleted())
                .orElseThrow(() -> new DataNotFoundException("No organization found."));

        organization.setDeleted(true);
        organizationRepository.save(organization);
        saveOrganizationHistory(organization, "D", true, "all");
    }

    @Override
    public List<OrganizationResponse> getAllOrganization() {
        // ✅ filter at DB level instead of fetching all and streaming
        return organizationRepository.findAllByDeletedFalse()
                .stream()
                .map(OrganizationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public OrganizationResponse getOrganization(Long organizationId, int back) throws DataNotFoundException {
        Organization organization = organizationRepository.findById(organizationId)
                .filter(org -> !org.getDeleted())
                .orElseThrow(() -> new DataNotFoundException("No organization found."));

        OrganizationHistory organizationHistory = (back > 0)
                ? organizationHistoryRepository.findLastModified(organizationId)
                : null;

        return new OrganizationResponse(organization, organizationHistory);
    }

    @Override
    @Transactional
    public UpdateModuleRequest updateModule(UpdateModuleRequest request) {
        Organization organization = organizationRepository.findById(request.getOrgId())
                .orElseThrow(() -> new RuntimeException("No organization found"));

        List<ModuleOrganization> existingModules = organization.getModules();
        boolean isCategoryDeleted = existingModules.stream().anyMatch(m -> m.getFieldId() != null);

        existingModules.forEach(moduleOrganizationRepository::delete);
        existingModules.clear();

        List<ModuleOrganization> newModules = request.getOrgModules().stream()
                .flatMap(orgModule -> orgModule.getCategories().stream()
                        .flatMap(category -> category.getFieldIds().stream()
                                .map(field -> {
                                    ModuleOrganization mo = new ModuleOrganization();
                                    mo.setModuleId(orgModule.getModuleId());
                                    mo.setCategoryId(category.getCategoryId());
                                    mo.setFieldId(field);
                                    mo.setOrganization(organization);
                                    return mo;
                                })))
                .collect(Collectors.toList());

        organization.getModules().addAll(newModules);
        organizationRepository.save(organization);

        if (isCategoryDeleted)
            saveOrganizationHistory(organization, "U", true, "category");

        return request;
    }

    @Override
    public UpdateModuleRequest getModules(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new DataNotFoundException("No organization found"));

        List<ModuleOrganization> moduleRows = moduleOrganizationRepository.getByOrganizationId(organizationId)
                .stream()
                .filter(m -> m.getFieldId() != null)
                .collect(Collectors.toList());

        Map<Long, Map<Long, List<Long>>> grouped = new LinkedHashMap<>();
        for (ModuleOrganization row : moduleRows) {
            grouped
                    .computeIfAbsent(row.getModuleId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(row.getCategoryId(), k -> new ArrayList<>())
                    .add(row.getFieldId());
        }

        List<OrgModuleRequest> orgModules = grouped.entrySet().stream()
                .map(moduleEntry -> {
                    OrgModuleRequest orgModule = new OrgModuleRequest();
                    orgModule.setModuleId(moduleEntry.getKey());
                    List<OrgCategoryRequest> categories = moduleEntry.getValue().entrySet().stream()
                            .map(categoryEntry -> {
                                OrgCategoryRequest category = new OrgCategoryRequest();
                                category.setCategoryId(categoryEntry.getKey());
                                category.setOrgId(organizationId);
                                category.setFieldIds(categoryEntry.getValue());
                                return category;
                            })
                            .collect(Collectors.toList());
                    orgModule.setCategories(categories);
                    return orgModule;
                })
                .collect(Collectors.toList());

        UpdateModuleRequest response = new UpdateModuleRequest();
        response.setOrgId(organization.getId());
        response.setOrgModules(orgModules);
        return response;
    }

    @Override
    @Transactional
    public UpdateModuleRequest updateModuleView(UpdateModuleRequest request) throws ResourceNotFoundException {
        Organization organization = organizationRepository.findById(request.getOrgId())
                .orElseThrow(() -> new RuntimeException("No organization found"));

        List<ModuleOrganization> existingModules = organization.getModules();
        boolean isViewDeleted = existingModules.stream().anyMatch(m -> m.getViewFieldId() != null);

        existingModules.forEach(moduleOrganizationRepository::delete);
        existingModules.clear();

        // ✅ Fixed: was using forEach inside map — only last field survived per module
        List<ModuleOrganization> newModules = request.getOrgModules().stream()
                .flatMap(orgModule -> orgModule.getCategories().stream()
                        .flatMap(category -> category.getFieldIds().stream()
                                .map(field -> {
                                    ModuleOrganization mo = new ModuleOrganization();
                                    mo.setModuleId(orgModule.getModuleId());
                                    mo.setViewCategoryId(category.getCategoryId());
                                    mo.setViewFieldId(field);
                                    mo.setOrganization(organization);
                                    return mo;
                                })))
                .collect(Collectors.toList());

        organization.getModules().addAll(newModules);
        organizationRepository.save(organization);

        if (isViewDeleted)
            saveOrganizationHistory(organization, "U", true, "view");

        return request;
    }

    @Override
    public ModuleRightRequest updateRight(ModuleRightRequest request) throws ResourceNotFoundException {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .filter(org -> !org.getDeleted())
                .orElseThrow(() -> new DataNotFoundException("No organization found."));

        List<Long> rightIds = request.getModuleRights().stream()
                .flatMap(mr -> mr.getRightIds().stream())
                .collect(Collectors.toList());

        List<Right> foundRights = rightRepository.findAllByRightIds(rightIds);

        // ✅ Proper copy to avoid mutating the same reference
        List<Right> removedRights = new ArrayList<>(organization.getRights());
        removedRights.removeAll(foundRights);

        organization.setRights(foundRights);
        organizationRepository.save(organization);

        if (!removedRights.isEmpty())
            saveRightOrganizationHistory(removedRights, organization.getId(), "U");

        return request;
    }

    @Override
    public boolean isEmailAlreadyExists(String email) {
        return userRepository.existsByEmail(email) 
            || userDetailRepository.existsByEmail(email);
    }

    // --- private helpers ---

    public void saveOrganizationHistory(Organization organization, String operation,
                                        boolean moduleChange, String moduleType) {
        OrganizationHistory org = new OrganizationHistory();
        org.setOrganizationId(organization.getId());
        org.setClientIP(organization.getClientIP());
        org.setCreatedAt(organization.getCreatedAt());
        org.setCreatedBy(organization.getCreatedBy());
        org.setOperation(operation);
        org.setName(organization.getName());
        org.setOrganizationLogoImageUrl(organization.getOrganizationLogoImageUrl());

        Plan plan = organization.getPlan();
        PlanHistory planHistory = new PlanHistory();
        planHistory.setPlanDescription(plan == null ? null : plan.getPlanDescription());
        planHistory.setPlanName(plan == null ? null : plan.getPlanName());
        planHistory.setPlanId(plan == null ? null : plan.getId());
        planHistory.setOperation(operation);
        org.setPlan(planHistory);

        if (moduleChange) {
            organization.getModules().stream()
                    .filter(module -> "all".equals(moduleType)
                            || ("view".equals(moduleType) && module.getViewFieldId() != null)
                            || ("category".equals(moduleType) && module.getFieldId() != null))
                    .forEach(module -> {
                        ModuleOrganizationHistory moh = new ModuleOrganizationHistory();
                        moh.setCategoryId(module.getCategoryId());
                        moh.setFieldId(module.getFieldId());
                        moh.setViewCategoryId(module.getViewCategoryId());
                        moh.setViewFieldId(module.getViewFieldId());
                        moh.setModuleId(module.getModuleId());
                        org.getModules().add(moh);
                    });
        }

        organizationHistoryRepository.save(org);
    }

    private void saveRightOrganizationHistory(List<Right> rights, Long organizationId, String operation) {
        List<RightOrganizationHistory> history = rights.stream()
                .map(right -> {
                    RightOrganizationHistory roh = new RightOrganizationHistory();
                    roh.setOrganizationId(organizationId);
                    roh.setRightId(right.getId());
                    roh.setOperation(operation);
                    return roh;
                })
                .collect(Collectors.toList());

        rightOrganizationHistoryRepository.saveAll(history);
    }

	 
}
