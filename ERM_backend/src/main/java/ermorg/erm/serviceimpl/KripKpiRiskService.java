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
	private RiskRepository riskRepository;

	@Autowired
	private RiskAsessmentRepository riskAsessmentRepository;

	@Override
	public KriKpiReviewResponseDTO save(KriKpiReviewRequestDTO request) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		User owner = userRepository.findById(request.getRiskOwner()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No user found for selected owner."));

		Risk risk = riskRepository.findById(request.getRiskId()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No risk found for selected risk."));

		RiskAssessment riskAssessment = riskAsessmentRepository
				.findByIdAndOrganizationIdAndDeletedFalse(request.getRiskAssessmentId(), organization.getId())
				.orElseThrow(() -> new ResourceNotFoundException("No risk assessment found for selected assessment."));

		if (riskAssessment.getRisk() == null || !riskAssessment.getRisk().getId().equals(risk.getId())) {
			throw new ResourceNotFoundException("Selected risk assessment does not belong to the selected risk.");
		}
		
		User evaluationBy = userRepository.findById(request.getKriEvaluationBy()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No user found for selected evaluation by."));

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

		List<SubRisk> subRisks = risk.getSubRisk().stream().filter(r-> request.getSubRiskIds().contains(r.getId()))
				.map(sbRisk->{
					sbRisk.setKriKpiReview(kriKpiReview);
					return sbRisk;
				})
				 .collect(Collectors.toList());
		
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		mapper.map(request, kriKpiReview);
		kriKpiReview.setOrganization(organization);
		kriKpiReview.setCompany(company);
		kriKpiReview.setRiskOwner(owner);
		kriKpiReview.setKriEvaluationBy(evaluationBy);
		kriKpiReview.setReporting(reporting);
		kriKpiReview.setRisk(risk);
		kriKpiReview.setRiskAssessment(riskAssessment);
		kriKpiReview.setSubRisks(subRisks);

		KriKpiReview saved = kriKpiReskRepository.save(kriKpiReview);

		return new KriKpiReviewResponseDTO(saved);
	}

	@Override
	public KriKpiReviewResponseDTO get(Long kriId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		KriKpiReview kriKpiReview = kriKpiReskRepository.getByOrgIdAndKriId(organization.getId(), kriId);
		if (kriKpiReview == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		return new KriKpiReviewResponseDTO(kriKpiReview);
	}

	@Override
	public List<CustomResponse> getView(Long kriId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		KriKpiReview kriKpiReview = kriKpiReskRepository.getByOrgIdAndKriId(organization.getId(), kriId);
		if (kriKpiReview == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		List<CustomResponse> customResponse = customResponseMapper.map("kriKpiReview", 1l,
				new KriKpiReviewResponseDTO(kriKpiReview), false);

		return customResponse;
	}

	@Override
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();

		List<KriKpiReview> kriKpiReviewlist = kriKpiReskRepository.getByOrgId(organization.getId());

		List<List<CustomResponse>> responseList = new ArrayList<>();
		for (KriKpiReview kriKpiReview : kriKpiReviewlist) {
			List<CustomResponse> customResponse = customResponseMapper.map("kriKpiReview", 1l,
					new KriKpiReviewResponseDTO(kriKpiReview),true);
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
