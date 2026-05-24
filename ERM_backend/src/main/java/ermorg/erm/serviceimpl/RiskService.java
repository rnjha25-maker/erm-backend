package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ermorg.erm.dto.response.AllRiskDropdownResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskAssessmentResponse;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.riskDTO.RiskAsessmentDto;
import ermorg.erm.dto.riskDTO.RiskDTO;
import ermorg.erm.dto.riskDTO.SubRiskDTO;
import ermorg.erm.dto.riskDTO.UpdateRiskStatusRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Branch;
import ermorg.erm.model.Company;
import ermorg.erm.model.Department;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.model.history.RiskHistory;
import ermorg.erm.model.history.SubRiskHistory;
import ermorg.erm.repository.BranchRepository;
import ermorg.erm.repository.LanguageRepository;
import ermorg.erm.repository.RiskAsessmentRepository;
import ermorg.erm.repository.RiskHistoryRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskTranslationHistoryRepository;
import ermorg.erm.repository.RiskTranslationRepository;
import ermorg.erm.repository.SubRiskHistoryRepository;
import ermorg.erm.repository.SubRiskRepository;
import ermorg.erm.repository.UserHistoryRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IRiskService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;
import ermorg.erm.util.mapper.CustomResponseMapperUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiskService implements IRiskService {

	@Autowired
	private RiskRepository riskRepository;

	@Autowired
	SubRiskRepository subRiskRepository;
	@Autowired
	private RiskHistoryRepository riskHistoryRepository;
	@Autowired
	private SubRiskHistoryRepository subRiskHistoryRepository;
	@Autowired
	private UserHistoryRepository userHistoryRepository;

	@Autowired
	private AuditorAware auditAware;

	@Autowired
	private LanguageRepository languageRepository;

	@Autowired
	private RiskTranslationRepository riskTranslationRepository;

	@Autowired
	private RiskTranslationHistoryRepository riskTranslationHistoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${erm.command-organization.service-id:ERM-COMMAND-ORGANIZATION}")
	private String commandOrganizationServiceId;

	@Autowired
	private RiskAsessmentRepository riskAsessmentRepository;

	@Autowired
	private FieldService fieldService;
	
	@Autowired
	private CustomResponseMapper customResponseMapper;

	@Override
	@Transactional
	public RiskResponse addRisk(RiskDTO request) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
//		String clientIp = ((AuditorAwareImpl) auditAware).getClientIp();
		Optional<Risk> riskOptional = riskRepository.findById(request.getRiskId()).filter(r -> !r.getDeleted());
		Risk newRisk = new Risk();
		Risk risk = riskOptional.orElse(newRisk);

		User owner = userRepository.findById(request.getRiskOwnerId()).filter(u -> !u.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("User not found for selected owner."));

		User riskChampion = userRepository.findById(request.getRiskChampionId()).filter(u -> !u.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("User not found for selected owner."));

		Branch branch = branchRepository.findById(request.getBranchId()).filter(b -> !b.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found for selected branch."));

		risk.setOrganizationId(organization.getId());
		risk.setCompanyId(company.getId());
		risk.setRisktitle(request.getRisktitle());
		risk.setRiskSource(request.getRiskSource());
		risk.setCategory(request.getCategory());
		risk.setSubCategory(request.getSubCategory());
		risk.setExposure(request.getExposure());
		risk.setFunction(request.getFunction());
		risk.setBusinessVertical(request.getBusinessVertical());
		risk.setBusinessSegment(request.getBusinessSegment());

		risk.setRiskOwner(owner);
		risk.setRiskChampion(riskChampion);

		risk.setRiskStatus(request.getRiskStatus());

		risk.setEvidanceRequired(request.getEvidanceRequired());
		risk.setSupportingEvidance(request.getSupportingEvidance());

		risk.setBranchId(branch.getId());
		//setting new fields
		
//				
//		risk.setRequesterIP(clientIp);
		List<SubRisk> subRiskList = new ArrayList<>();

		for (SubRiskDTO subRiskDTO : request.getSubRisk()) {
			SubRisk subRisk = new SubRisk();
			if (subRiskDTO.getSubRiskId() != null) {
				subRisk = subRiskRepository.findById(subRiskDTO.getSubRiskId()).orElse(new SubRisk());
			}

			subRisk.setSubRisk(subRiskDTO.getSubRiskTitle());
			subRisk.setRisk(risk);

			subRisk.setOrganizationId(organization.getId());
			subRisk.setCompanyId(company.getId());

			subRiskList.add(subRisk);
		}

//		Language language = languageRepository.getLanguageByLanguageCode("en");
//		RiskTranslation riskTranslation = riskTranslationRepository
//				.findById(risk.getRiskTranslation() == null ? 0 : risk.getRiskTranslation().getId())
//				.orElse(new RiskTranslation());

//		riskTranslation.setLanguage(language);
//		riskTranslation.setRisk(risk);
//		risk.setRiskTranslation(riskTranslation);
		risk.setSubRisk(subRiskList);
		Risk savedRisk = riskRepository.save(risk);

		if (request.getRiskId() != 0) {
			captureHistory(risk, "U");

		}

		return toRiskResponse(savedRisk);
	}

	@Override
	@Transactional
	public RiskResponse addRiskAssessment(RiskAsessmentDto request) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();

		Risk risk = riskRepository.findById(request.getRiskId())
				.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));

		RiskAssessment riskAsessment = riskAsessmentRepository.findById(request.getAssessmentId())
				.filter(as -> !as.getDeleted()).orElse(new RiskAssessment());

		SubRisk subRisk = null;
		if (request.getSubRiskId() != null) {
			subRisk = risk.getSubRisk().stream()
					.filter(existingSubRisk -> request.getSubRiskId().equals(existingSubRisk.getId()))
					.findFirst()
					.orElseThrow(() -> new ResourceNotFoundException(
							"Selected sub risk does not belong to the selected risk."));
		}

		riskAsessment.setOrganization(organization);
		riskAsessment.setCompany(company);
		riskAsessment.setRisk(risk);
		riskAsessment.setSubRisk(subRisk);
		riskAsessment.setCustomerImpact(request.getCustomerImpact());
		riskAsessment.setRiskAnalysisType(request.getRiskAnalysisType());
		riskAsessment.setLikelihood(request.getLikelihood());
		riskAsessment.setLikelihoodProbability(request.getLikelihoodProbability());
		riskAsessment.setFinancialImpact(request.getFinancialImpact());
		riskAsessment.setOperationalImpact(request.getOperationalImpact());
		riskAsessment.setCustomerImpact(request.getCustomerImpact());
		riskAsessment.setReputationalImpact(request.getReputationalImpact());
		riskAsessment.setLegalComplianceImpact(request.getLegalComplianceImpact());
		riskAsessment.setGrossImpactScore(request.getGrossImpactScore());
		riskAsessment.setRiskRating(request.getRiskRating());
		riskAsessment.setVelocity(request.getVelocity());
		riskAsessment.setRiskAppetite(request.getRiskAppetite());
		riskAsessment.setRiskToleranceStatus(request.getRiskToleranceStatus());
		riskAsessment.setRiskPriority(request.getRiskPriority());
		riskAsessment.setRiskTreatmentStrategy(request.getRiskTreatmentStrategy());
		riskAsessment.setRiskAssessmentFrequency(request.getRiskAssessmentFrequency());
		riskAsessment.setRiskAssessmentBy(request.getRiskAssessmentBy());
		riskAsessment.setRiskReporting(request.getRiskReporting());
		riskAsessment.setAssetValue(request.getAssetValue());
		riskAsessment.setOffPotentialLoss(request.getOffPotentialLoss());
		riskAsessment.setYearlyFrequency(request.getYearlyFrequency());
		riskAsessment.setYearlyLossExpectancy(request.getYearlyLossExpectancy());
		riskAsessment.setRiskRatingScore(request.getRiskRatingScore());
		riskAsessment.setResidualRiskRatingCriteria(request.getResidualRiskRatingCriteria());
		RiskAssessment saveAssessment = riskAsessmentRepository.save(riskAsessment);

		return toRiskResponse(saveAssessment.getRisk());
	}

	@Transactional
	public List<SubRisk> saveAllSubRisks(List<SubRisk> subRisks) {

		return subRiskRepository.saveAll(subRisks);
	}

	public RiskResponse getRisk(Long id) throws ResourceNotFoundException {
		Risk risk = riskRepository.findById(id).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));

		return toRiskResponse(risk);
	}

	public void deleteRisk(Long id) throws ResourceNotFoundException {

		Risk risk = riskRepository.findById(id).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));
		risk.setDeleted(true);
		riskRepository.save(risk);

		captureHistory(risk, "D");
	}

	/*
	 * public List<List<CustomResponse>> getAllRisks() throws
	 * ResourceNotFoundException {
	 * 
	 * Organization organization = OrganizationContext.getOrganization(); List<Risk>
	 * riskList = riskRepository.getAllRisksForOrgNoPage(organization.getId());
	 * List<List<CustomResponse>> responseList = new ArrayList<>();
	 * 
	 * for(Risk risk : riskList) { List<CustomResponse> response =
	 * customResponseMapper.map("risk", 1l, new RiskResponse(risk), true);
	 * 
	 * responseList.add(response); }
	 * 
	 * 
	 * return responseList; }
	 */

	@Override
	@Transactional
	public Page<List<CustomResponse>> getAllRisks(Pageable pageable) {

		Organization organization = OrganizationContext.getOrganization();

		Page<Risk> riskPage = riskRepository.getAllRisksByOrganizationId(organization.getId(), pageable);

		Map<Long, String> functionNames = loadFunctionNames(riskPage.getContent());
		Map<Long, String> branchNames = loadBranchNames(riskPage.getContent());
		Map<String, String> businessSegmentNames = loadBusinessSegmentNames(riskPage.getContent());
		Map<Long, String> businessVerticalNames = loadBusinessVerticalNames(riskPage.getContent());

		return riskPage.map(risk -> mapRisk(risk, functionNames, branchNames, businessSegmentNames, businessVerticalNames));
	}

	private List<CustomResponse> mapRisk(Risk risk) {

		return customResponseMapper.map("risk", 1L, toRiskResponse(risk), false);
	}

	private List<CustomResponse> mapRisk(Risk risk, Map<Long, String> functionNames, Map<Long, String> branchNames) {

		return customResponseMapper.map("risk", 1L, toRiskResponse(risk, functionNames, branchNames), false);
	}

	private List<CustomResponse> mapRisk(Risk risk, Map<Long, String> functionNames, Map<Long, String> branchNames,
			Map<String, String> businessSegmentNames, Map<Long, String> businessVerticalNames) {

		return customResponseMapper.map("risk", 1L,
				toRiskResponse(risk, functionNames, branchNames, businessSegmentNames, businessVerticalNames), false);
	}

	public void captureHistory(Risk risk, String actionType) {

		RiskHistory riskHistory = new RiskHistory();

		riskHistory.setOperation(actionType);
		copyFromRisk(risk, riskHistory);

		riskHistoryRepository.save(riskHistory);
	}

	private void copyFromRisk(Risk risk, RiskHistory riskHistory) {
		riskHistory.setRisktitle(risk.getRisktitle());
		riskHistory.setRiskSource(risk.getRiskSource());
		riskHistory.setCategory(risk.getCategory());
		riskHistory.setSubCategory(risk.getSubCategory());
		riskHistory.setExposure(risk.getExposure());
		riskHistory.setFunction(risk.getFunction());
		riskHistory.setBusinessVertical(risk.getBusinessVertical());
		riskHistory.setBusinessSegment(risk.getBusinessSegment());

		riskHistory.setRiskOwner(risk.getRiskOwner());
		riskHistory.setRiskChampion(risk.getRiskChampion());

		riskHistory.setRiskStatus(risk.getRiskStatus());

		riskHistory.setEvidanceRequired(risk.getEvidanceRequired());
		riskHistory.setSupportingEvidance(risk.getSupportingEvidance());

//		riskHistory.setBranchId(risk.getBranch());

		List<SubRiskHistory> subRiskHistoryList = risk.getSubRisk().stream().map(subRisk -> {
			SubRiskHistory subRiskHistory = new SubRiskHistory();
			subRiskHistory.setSubRisk(subRisk.getSubRisk());
			subRiskHistory.setRisk(risk);
			subRiskHistory.setOperation(riskHistory.getOperation());
			return subRiskHistory;
		}).collect(Collectors.toList());
		riskHistory.setSubRiskHostory(subRiskHistoryList);
	}

	@Override
	public List<CustomResponse> getRiskView(Long riskId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) { 
			throw new ResourceNotFoundException("Organization not found");
		}

		List<CustomResponse>  customResponses = new ArrayList<>();
		Risk risk = riskRepository.getRisksByOrgIdAndRiskId(organization.getId(), riskId);// .orElseThrow(() -> new
																				// ResourceNotFoundException("Risk not
																				// found."));
		if (risk == null) {
			throw new ResourceNotFoundException("Risk not found");
		}
		RiskResponse riskResponse = toRiskResponse(risk);
		List<CustomFieldResponse> customFieldResponse = fieldService.getCustomFieldResponse(1, "risk");
		
		customFieldResponse.stream().forEach(field -> {
			CustomResponse customResponse;
			try {
				customResponse = CustomResponseMapperUtil.map(riskResponse, field, "risk");
				customResponses.add(customResponse);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error("getRiskView() {}", e.getMessage());
			}
		});
		return customResponses;
	}
	
	@Override
	public List<CustomResponse> getRisAssessmentkView(Long assessmentId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) {
			throw new ResourceNotFoundException("Organization not found");
		}

		RiskAssessment risk = riskAsessmentRepository.getAssessmentByOrgIdAndAssessmentId(organization.getId(), assessmentId);// .orElseThrow(() -> new
		if (risk == null) {
			throw new ResourceNotFoundException("Risk assessment not found");
		}
																				
		return mapRiskAssessment(risk);
	}
	
	@Override	@Transactional(readOnly = true)	
	public Page<List<CustomResponse>> getAllAssessment(Pageable pageable) {

	    Organization organization = OrganizationContext.getOrganization();

	    Page<Long> assessmentIdPage =
	            riskAsessmentRepository.getAllIdsByOrgId(organization.getId(), pageable);

	    if (assessmentIdPage.isEmpty()) {
	        return Page.empty(pageable);
	    }

	    List<RiskAssessment> assessments = riskAsessmentRepository.getAllByOrgIdAndIds(
	            organization.getId(),
	            assessmentIdPage.getContent()
	    );

	    Map<Long, RiskAssessment> assessmentById = assessments.stream()
	            .collect(Collectors.toMap(RiskAssessment::getId, Function.identity()));

	    List<List<CustomResponse>> responseList = assessmentIdPage.getContent().stream()
	            .map(assessmentById::get)
	            .filter(java.util.Objects::nonNull)
	            .map(this::mapRiskAssessment)
	            .collect(Collectors.toList());

	    return new PageImpl<>(responseList, pageable, assessmentIdPage.getTotalElements());
	}
	
	private List<CustomResponse> mapRiskAssessment(RiskAssessment riskAssessment) {

	    RiskAssessmentResponse response = new RiskAssessmentResponse(riskAssessment);
	    List<CustomResponse> customResponses = new ArrayList<>(customResponseMapper.map(
	                    "riskAssessment",
	                    1L,
	                    response,
	                    false
	            ));

	    boolean hasSubRiskField = customResponses.stream()
	    		.map(CustomResponse::getFieldName)
	    		.filter(java.util.Objects::nonNull)
	    		.map(fieldName -> fieldName.toLowerCase().replaceAll("[^a-z0-9]", ""))
	    		.anyMatch(fieldName -> fieldName.contains("subrisk") || fieldName.contains("risksubtitle"));

	    if (!hasSubRiskField) {
	    	CustomResponse subRiskResponse = new CustomResponse();
	    	subRiskResponse.setFieldName("Risk Sub Title");
	    	subRiskResponse.setFieldType("Dropdown");
	    	subRiskResponse.setValue(response.getSubRiskName());
	    	customResponses.add(subRiskResponse);
	    }

	    return customResponses;
	}

	@Override
	public RiskResponse updateRiskStatus(UpdateRiskStatusRequest request) throws ResourceNotFoundException {
		// Validate status value
		String status = request.getStatus();
		if (status == null || (!status.equals("APPROVED") && !status.equals("REJECT"))) {
			throw new ResourceNotFoundException("Invalid status. Status must be either 'APPROVED' or 'REJECT'.");
		}
		
		// Find the risk by ID
		Risk risk = riskRepository.findById(request.getRiskId())
				.filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));
		
		// Update the risk status
		risk.setRiskStatus(status);
		
		// Save the risk
		Risk savedRisk = riskRepository.save(risk);
		
		// Capture history for the update
		captureHistory(risk, "U");
		
		return toRiskResponse(savedRisk);
	}

	private RiskResponse toRiskResponse(Risk risk) {
		if (risk == null) {
			return null;
		}

		Map<Long, String> functionNames = risk.getFunction() != null
				? loadFunctionNames(List.of(risk))
				: Collections.emptyMap();
		Map<Long, String> branchNames = risk.getBranchId() != null
				? loadBranchNames(List.of(risk))
				: Collections.emptyMap();
		Map<String, String> businessSegmentNames = risk.getBusinessSegment() != null
				? loadBusinessSegmentNames(List.of(risk))
				: Collections.emptyMap();
		Map<Long, String> businessVerticalNames = risk.getBusinessVertical() != null
				? loadBusinessVerticalNames(List.of(risk))
				: Collections.emptyMap();

		return toRiskResponse(risk, functionNames, branchNames, businessSegmentNames, businessVerticalNames);
	}

	private RiskResponse toRiskResponse(Risk risk, Map<Long, String> functionNames, Map<Long, String> branchNames) {
		return toRiskResponse(risk, functionNames, branchNames, Collections.emptyMap(), Collections.emptyMap());
	}

	private RiskResponse toRiskResponse(Risk risk, Map<Long, String> functionNames, Map<Long, String> branchNames,
			Map<String, String> businessSegmentNames, Map<Long, String> businessVerticalNames) {
		RiskResponse response = new RiskResponse(risk);

		response.setFunctionName(functionNames.get(response.getFunction()));
		response.setBranchName(response.getBranchId() != null ? branchNames.get(response.getBranchId()) : null);
		response.setBusinessSegmentName(businessSegmentNames.getOrDefault(response.getBusinessSegment(),
				response.getBusinessSegment()));
		response.setBusinessVerticalName(response.getBusinessVertical() != null
				? businessVerticalNames.getOrDefault(response.getBusinessVertical(), response.getBusinessVertical().toString())
				: null);

		return response;
	}

	private Map<Long, String> loadFunctionNames(List<Risk> risks) {
		Set<Long> functionIds = risks.stream()
				.map(Risk::getFunction)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		if (functionIds.isEmpty()) {
			return Collections.emptyMap();
		}

		return departmentRepository.findAllById(functionIds).stream()
				.filter(department -> !department.getDeleted())
				.collect(Collectors.toMap(Department::getId, Department::getName));
	}

	private Map<Long, String> loadBranchNames(List<Risk> risks) {
		Set<Long> branchIds = risks.stream()
				.map(Risk::getBranchId)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		if (branchIds.isEmpty()) {
			return Collections.emptyMap();
		}

		return branchRepository.findAllById(branchIds).stream()
				.filter(branch -> !branch.getDeleted())
				.collect(Collectors.toMap(Branch::getId, Branch::getName));
	}

	private Map<String, String> loadBusinessSegmentNames(List<Risk> risks) {
		Set<String> segmentIds = risks.stream()
				.map(Risk::getBusinessSegment)
				.filter(java.util.Objects::nonNull)
				.filter(this::isNumeric)
				.collect(Collectors.toSet());

		if (segmentIds.isEmpty()) {
			return Collections.emptyMap();
		}

		return segmentIds.stream()
				.collect(Collectors.toMap(Function.identity(), this::resolveBusinessSegmentName));
	}

	private Map<Long, String> loadBusinessVerticalNames(List<Risk> risks) {
		Set<Long> verticalIds = risks.stream()
				.map(Risk::getBusinessVertical)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		if (verticalIds.isEmpty()) {
			return Collections.emptyMap();
		}

		return verticalIds.stream()
				.collect(Collectors.toMap(Function.identity(), this::resolveBusinessVerticalName));
	}

	private String resolveBusinessSegmentName(String segmentId) {
		return resolveOrganizationLookupName(
				String.format("http://%s/business-segment/%s", commandOrganizationServiceId, segmentId),
				"businessSegmentName",
				"segmentName",
				segmentId);
	}

	private String resolveBusinessVerticalName(Long verticalId) {
		return resolveOrganizationLookupName(
				String.format("http://%s/business-vertical/%d", commandOrganizationServiceId, verticalId),
				"businessVerticalName",
				"verticalName",
				verticalId.toString());
	}

	private String resolveOrganizationLookupName(String url, String preferredNameField, String fallbackNameField,
			String fallbackValue) {
		try {
			String body = restTemplate.getForObject(url, String.class);
			JsonNode data = objectMapper.readTree(body).path("data");
			String name = data.path(preferredNameField).asText(null);
			if (name == null || name.isBlank()) {
				name = data.path(fallbackNameField).asText(null);
			}
			return (name == null || name.isBlank()) ? fallbackValue : name;
		} catch (RestClientException | java.io.IOException ex) {
			log.warn("Unable to resolve organization lookup from {}: {}", url, ex.getMessage());
			return fallbackValue;
		}
	}

	private boolean isNumeric(String value) {
		return value != null && value.matches("\\d+");
	}

	
	@Override
	@Transactional(readOnly = true)
	public Page<AllRiskDropdownResponse> getAllRiskDropdown(Pageable pageable) {

	    Organization organization = OrganizationContext.getOrganization();

	    if (organization == null) {
	        throw new RuntimeException("Organization not found");
	    }

	    Page<Risk> riskPage =
	            riskRepository.getAllRisksByOrgId(organization.getId(), pageable);

	    return riskPage.map(AllRiskDropdownResponse::new);
	}
	 
}
