package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.dto.riskDTO.RiskReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskReview;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskReviewRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.IRiskReviewService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.SubRiskSelectionUtil;
import ermorg.erm.util.mapper.CustomResponseMapper;

@Service
public class RiskReviewService implements IRiskReviewService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RiskRepository riskRepository;
	
	@Autowired
	private RiskReviewRepository riskReviewRepository;
	
	@Autowired
	private CustomResponseMapper customResponseMapper;
	@Override
	public RiskReviewResponseDtoResponse saveRiskReview(RiskReviewRequestDTO request) throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		validateRequiredId(request.getRiskId(), "Please select risk.");
		validateRequiredId(request.getRiskReporting(), "Please select risk reporting.");
		
		Risk risk = java.util.Optional.ofNullable(riskRepository.getRisksByOrgIdAndRiskId(organization.getId(), request.getRiskId()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));
				
			User riskReporting = userRepository.findById(request.getRiskReporting())
			.filter(u -> !u.getDeleted())
			.orElseThrow(()-> new ResourceNotFoundException("User not found for selected risk reporting."));
			
			RiskReview riskReview;
			if (request.getRiskReviewId() == null || request.getRiskReviewId() == 0) {
				// Create new entity for new risk reviews
				riskReview = new RiskReview();
			} else {
				// Find existing entity for updates
				riskReview = riskReviewRepository.findById(request.getRiskReviewId())
					.filter(r -> !r.getDeleted())
					.orElseThrow(() -> new ResourceNotFoundException("RiskReview not found."));
			}
			
			createMapper().map(request, riskReview);
			riskReview.setResidualRiskRating(resolveFirstText(
					request.getResidualRiskRating(), request.getResidualRiskRatingCriteria()));
			
			// Ensure new entities have id = null (ModelMapper might have copied the ID)
			if (request.getRiskReviewId() == null || request.getRiskReviewId() == 0) {
				riskReview.setId(null);
			}
				
				List<SubRisk> subRisks = SubRiskSelectionUtil.resolveSelectedSubRisks(risk, request.getSubRiskIds()).stream()
						.map(sbRisk->{
							sbRisk.setRiskReview(riskReview);
							return sbRisk;
						})
						 .collect(Collectors.toList());
				riskReview.setSubRisks(subRisks);
				riskReview.setRisk(risk);
				riskReview.setRiskReporting(riskReporting);
				riskReview.setCurrency(request.getCurrency());
				riskReview.setOrganization(organization);
				riskReview.setCompany(company);
				RiskReview saved = riskReviewRepository.save(riskReview);
		return new RiskReviewResponseDtoResponse(saved);
	}

	private void validateRequiredId(long id, String message) throws ResourceNotFoundException {
		if (id <= 0) {
			throw new ResourceNotFoundException(message);
		}
	}

	private ModelMapper createMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setPreferNestedProperties(false);
		mapper.typeMap(RiskReviewRequestDTO.class, RiskReview.class).addMappings(mapping -> {
			mapping.skip(RiskReview::setRisk);
			mapping.skip(RiskReview::setRiskReporting);
			mapping.skip(RiskReview::setOrganization);
			mapping.skip(RiskReview::setCompany);
			mapping.skip(RiskReview::setSubRisks);
		});
		return mapper;
	}

	private String resolveFirstText(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	@Override
	public RiskReviewResponseDtoResponse get(Long rieviewId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		RiskReview riskRivew = riskReviewRepository.getByOrgAndReivewId(organization.getId(), rieviewId);
		if(riskRivew == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		return new RiskReviewResponseDtoResponse(riskRivew);
	}

	@Override
	public List<CustomResponse> getView(Long rieviewId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		RiskReview riskRivew = riskReviewRepository.getByOrgAndReivewId(organization.getId(), rieviewId);
		if(riskRivew == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		List<CustomResponse> response = customResponseMapper.map("riskReview", 1l, new RiskReviewResponseDtoResponse(riskRivew), false);
		return response;
	}

	@Override
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		List<RiskReview> riskReviewList = riskReviewRepository.getByOrgId(organization.getId());
		
		List<List<CustomResponse>> responseList = new ArrayList<>();
		for(RiskReview riskReview : riskReviewList) {
			List<CustomResponse> response = customResponseMapper.map("riskReview", 1l, new RiskReviewResponseDtoResponse(riskReview),true);
			responseList.add(response);
		}
		return responseList;
	}

	@Override
	public void delete(Long rieviewId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		RiskReview riskRivew = riskReviewRepository.getByOrgAndReivewId(organization.getId(), rieviewId);
		if(riskRivew == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		riskRivew.setDeleted(true);
		
		riskReviewRepository.save(riskRivew);
	}
	
	
	

}
