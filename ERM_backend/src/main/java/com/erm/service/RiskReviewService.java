package com.erm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.RiskReviewResponseDtoResponse;
import com.erm.dto.riskDTO.RiskReviewRequestDTO;
import com.erm.exception.ResourceNotFoundException;
import com.erm.model.Company;
import com.erm.model.Organization;
import com.erm.model.Risk;
import com.erm.model.RiskReview;
import com.erm.model.SubRisk;
import com.erm.model.User;
import com.erm.repository.RiskRepository;
import com.erm.repository.RiskReviewRepository;
import com.erm.repository.UserRepository;
import com.erm.util.CompanyContext;
import com.erm.util.OrganizationContext;
import com.erm.util.mapper.CustomResponseMapper;

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
		
		Risk risk = riskRepository.findById(request.getRiskId())
				.filter(r -> !r.getDeleted())
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
			
			ModelMapper mapper = new ModelMapper();
			
			mapper.map(request, riskReview);
			
			// Ensure new entities have id = null (ModelMapper might have copied the ID)
			if (request.getRiskReviewId() == null || request.getRiskReviewId() == 0) {
				riskReview.setId(null);
			}
				
				List<SubRisk> subRisks = risk.getSubRisk().stream().filter(r-> request.getSubRiskIds().contains(r.getId()))
						.map(sbRisk->{
							sbRisk.setRiskReview(riskReview);
							return sbRisk;
						})
						 .collect(Collectors.toList());
				riskReview.setSubRisks(subRisks);
				riskReview.setRisk(risk);
				riskReview.setRiskReporting(riskReporting);
				riskReview.setOrganization(organization);
				riskReview.setCompany(company);
				RiskReview saved = riskReviewRepository.save(riskReview);
				RiskReviewResponseDtoResponse riskReviewResponseDTO = new RiskReviewResponseDtoResponse();
				
				mapper.map(request, riskReviewResponseDTO);
				
				riskReviewResponseDTO.setRiskReviewId(saved.getId());
		return riskReviewResponseDTO ;
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
