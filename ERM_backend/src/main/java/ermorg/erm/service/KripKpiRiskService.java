package ermorg.erm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.KriKpiReview;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.KriKpiRiskRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

@Service
public class KripKpiRiskService implements IKriKpiRiskService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private KriKpiRiskRepository kriKpiReskRepository;

	@Autowired
	private CustomResponseMapper customResponseMapper;
	
	@Autowired
	private RiskRepository riskRepository;

	@Override
	public KriKpiReviewResponseDTO save(KriKpiReviewRequestDTO request) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		User owner = userRepository.findById(request.getRiskOwner()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No user found for selected owner."));

		Risk risk = riskRepository.findById(request.getRiskId()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No risk found for selected risk."));
		
		User evaluationBy = userRepository.findById(request.getKriEvaluationBy()).filter(r -> !r.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No user found for selected evaluation by."));

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
		
		// Configure ModelMapper to skip the id field (kriId and id are the same, so ignore id during mapping)
		// Set a simple property condition to skip 'id' field (without chaining original condition to avoid syntax issues)
		mapper.getConfiguration().setPropertyCondition(context -> {
			String destPropertyName = context.getMapping().getLastDestinationProperty().getName();
			return !destPropertyName.equals("id");
		});
		
		// Now create or get TypeMap - the property condition will prevent id mapping
		TypeMap<KriKpiReviewRequestDTO, KriKpiReview> typeMap = mapper.getTypeMap(KriKpiReviewRequestDTO.class, KriKpiReview.class);
		if (typeMap == null) {
			typeMap = mapper.createTypeMap(KriKpiReviewRequestDTO.class, KriKpiReview.class);
		}
		
		// Reset property condition to default (null) after TypeMap creation
		mapper.getConfiguration().setPropertyCondition(null);
		
		mapper.map(request, kriKpiReview);
		kriKpiReview.setOrganization(organization);
		kriKpiReview.setCompany(company);
		kriKpiReview.setRiskOwner(owner);
		kriKpiReview.setKriEvaluationBy(evaluationBy);
		kriKpiReview.setRisk(risk);
		kriKpiReview.setSubRisks(subRisks);

		KriKpiReview saved = kriKpiReskRepository.save(kriKpiReview);

		KriKpiReviewResponseDTO kriKpiReviewResponseDTO = new KriKpiReviewResponseDTO();

		mapper.map(request, kriKpiReviewResponseDTO);

		kriKpiReviewResponseDTO.setKriId(saved.getId());

		return kriKpiReviewResponseDTO;
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
