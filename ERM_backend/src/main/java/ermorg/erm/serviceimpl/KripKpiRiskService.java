package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.Company;
import ermorg.erm.model.KriKpiReview;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.KriKpiRiskRepository;
import ermorg.erm.repository.RiskAsessmentRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.IKriKpiRiskService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.SubRiskSelectionUtil;
import ermorg.erm.util.mapper.CustomResponseMapper;

@Service
@Transactional
public class KripKpiRiskService implements IKriKpiRiskService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private KriKpiRiskRepository kriKpiReskRepository;

	@Autowired
	private CustomResponseMapper customResponseMapper;

	@Autowired
	private FieldMapperUtils fieldMapperUtils;
	
	@Autowired
	private RiskRepository riskRepository;

	@Autowired
	private RiskAsessmentRepository riskAsessmentRepository;

	@Override
	public KriKpiReviewResponseDTO save(KriKpiReviewRequestDTO request) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		validateRequiredId(request.getRiskId(), "Please select risk.");
		validateRequiredId(request.getRiskOwner(), "Please select risk owner.");

		User owner = userRepository.findById(request.getRiskOwner()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No user found for selected owner."));

		Risk risk = java.util.Optional.ofNullable(riskRepository.getRisksByOrgIdAndRiskId(organization.getId(), request.getRiskId()))
				.orElseThrow(() -> new ResourceNotFoundException("No risk found for selected risk."));

		List<SubRisk> subRisks = SubRiskSelectionUtil.resolveSelectedSubRisks(risk, request.getSubRiskIds());
		RiskAssessment riskAssessment = resolveRiskAssessment(request, organization, risk, subRisks);
		
		User evaluationBy = null;
		if (request.getKriEvaluationBy() > 0) {
			evaluationBy = userRepository.findById(request.getKriEvaluationBy()).filter(r -> !r.getDeleted())
					.orElseThrow(() -> new ResourceNotFoundException("No user found for selected evaluation by."));
		}

		User reporting = null;
		if (request.getReporting() > 0) {
			reporting = userRepository.findById(request.getReporting()).filter(r -> !r.getDeleted())
					.orElseThrow(() -> new ResourceNotFoundException("No user found for selected reporting."));
		}

		KriKpiReview kriKpiReview;
		if (request.getKriId() == 0) {
			// Create new entity for new KRI reviews
			kriKpiReview = new KriKpiReview();
		} else {
			// Find existing entity for updates
			kriKpiReview = kriKpiReskRepository.findById(request.getKriId())
				.filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("KriKpiReview not found."));
		}

		subRisks = subRisks.stream()
				.map(sbRisk -> {
					sbRisk.setKriKpiReview(kriKpiReview);
					return sbRisk;
				})
				 .collect(Collectors.toList());
		
		createMapper().map(request, kriKpiReview);
		kriKpiReview.setOrganization(organization);
		kriKpiReview.setCompany(company);
		kriKpiReview.setRiskOwner(owner);
		kriKpiReview.setKriEvaluationBy(evaluationBy);
		kriKpiReview.setReporting(reporting);
		kriKpiReview.setRisk(risk);
		kriKpiReview.setRiskAssessment(riskAssessment);
		kriKpiReview.setSubRisks(subRisks);

		KriKpiReview saved = kriKpiReskRepository.save(kriKpiReview);

		return toResponse(saved);
	}

	private RiskAssessment resolveRiskAssessment(KriKpiReviewRequestDTO request, Organization organization, Risk risk,
			List<SubRisk> subRisks) throws ResourceNotFoundException {
		if (request.getRiskAssessmentId() > 0) {
			RiskAssessment riskAssessment = riskAsessmentRepository
					.findByIdAndOrganizationIdAndDeletedFalse(request.getRiskAssessmentId(), organization.getId())
					.orElseThrow(() -> new ResourceNotFoundException("No risk assessment found for selected assessment."));
			validateAssessmentBelongsToSelection(riskAssessment, risk, subRisks);
			return riskAssessment;
		}

		if (subRisks.size() > 1) {
			throw new ResourceNotFoundException("Please select risk assessment for multiple sub risks.");
		}

		List<RiskAssessment> assessments = subRisks.isEmpty()
				? riskAsessmentRepository.findLatestByOrgIdAndRiskIdWithoutSubRisk(organization.getId(), risk.getId())
				: riskAsessmentRepository.findLatestByOrgIdAndRiskIdAndSubRiskId(
						organization.getId(), risk.getId(), subRisks.get(0).getId());

		if (assessments.isEmpty()) {
			throw new ResourceNotFoundException("No risk assessment found for selected risk and sub risk.");
		}
		return assessments.get(0);
	}

	private void validateAssessmentBelongsToSelection(RiskAssessment riskAssessment, Risk risk, List<SubRisk> subRisks)
			throws ResourceNotFoundException {
		if (riskAssessment.getRisk() == null || !riskAssessment.getRisk().getId().equals(risk.getId())) {
			throw new ResourceNotFoundException("Selected risk assessment does not belong to the selected risk.");
		}

		if (!subRisks.isEmpty()) {
			Long assessmentSubRiskId = riskAssessment.getSubRisk() != null ? riskAssessment.getSubRisk().getId() : null;
			boolean matched = subRisks.stream().anyMatch(subRisk -> subRisk.getId().equals(assessmentSubRiskId));
			if (!matched) {
				throw new ResourceNotFoundException("Selected risk assessment does not belong to the selected sub risk.");
			}
		}
	}

	private void validateRequiredId(long id, String message) throws ResourceNotFoundException {
		if (id <= 0) {
			throw new ResourceNotFoundException(message);
		}
	}

	/**
	 * Builds the response DTO and resolves the department ID stored in
	 * {@code businessFunction} to its human-readable name so that all
	 * endpoints — including the raw {@code GET /{id}} — return the name,
	 * not the numeric ID.
	 */
	private KriKpiReviewResponseDTO toResponse(KriKpiReview kriKpiReview) {
		KriKpiReviewResponseDTO dto = new KriKpiReviewResponseDTO(kriKpiReview);
		// The DTO constructor copies the raw businessFunction string (dept ID)
		// into departmentName. Resolve it to the actual name here.
		dto.setDepartmentName(fieldMapperUtils.resolveDepartmentFromObject(dto.getDepartmentName()));
		return dto;
	}

	private ModelMapper createMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setPreferNestedProperties(false);
		mapper.typeMap(KriKpiReviewRequestDTO.class, KriKpiReview.class).addMappings(mapping -> {
			mapping.skip(KriKpiReview::setRisk);
			mapping.skip(KriKpiReview::setRiskAssessment);
			mapping.skip(KriKpiReview::setRiskOwner);
			mapping.skip(KriKpiReview::setKriEvaluationBy);
			mapping.skip(KriKpiReview::setReporting);
			mapping.skip(KriKpiReview::setOrganization);
			mapping.skip(KriKpiReview::setCompany);
			mapping.skip(KriKpiReview::setSubRisks);
		});
		return mapper;
	}

	@Override
	public KriKpiReviewResponseDTO get(Long kriId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		KriKpiReview kriKpiReview = kriKpiReskRepository.getByOrgIdAndKriId(organization.getId(), kriId);
		if (kriKpiReview == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		return toResponse(kriKpiReview);
	}

	@Override
	public List<CustomResponse> getView(Long kriId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		KriKpiReview kriKpiReview = kriKpiReskRepository.getByOrgIdAndKriId(organization.getId(), kriId);
		if (kriKpiReview == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		List<CustomResponse> customResponse = customResponseMapper.map("kriKpiReview", 1l,
				toResponse(kriKpiReview), false);

		return customResponse;
	}

	@Override
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();

		List<KriKpiReview> kriKpiReviewlist = kriKpiReskRepository.getByOrgId(organization.getId());

		List<List<CustomResponse>> responseList = new ArrayList<>();
		for (KriKpiReview kriKpiReview : kriKpiReviewlist) {
			List<CustomResponse> customResponse = customResponseMapper.map("kriKpiReview", 1l,
					toResponse(kriKpiReview), true);
			responseList.add(customResponse);
		}

		return responseList;
	}
	
	@Override
	public void delete(Long kriId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		KriKpiReview kriKpiReview = kriKpiReskRepository.getByOrgIdAndKriId(organization.getId(), kriId);
		if (kriKpiReview == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		kriKpiReview.setDeleted(true);
		kriKpiReskRepository.save(kriKpiReview);
	}

}
