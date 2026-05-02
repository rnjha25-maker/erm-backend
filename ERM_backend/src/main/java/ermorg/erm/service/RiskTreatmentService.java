package ermorg.erm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskControl;
import ermorg.erm.model.RiskResponseTreatment;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskResponseTreatmentRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;
import ermorg.erm.util.mapper.CustomResponseMapperUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiskTreatmentService implements IRiskTreatmentService {

	@Autowired
	private RiskRepository riskRepository;
	
	@Autowired
	private RiskResponseTreatmentRepository riskResponseTreatmentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CustomResponseMapper customResponseMapper;
	
	@Autowired
	private FieldService fieldService;
	@Override
	public RiskResponseTreatmentResponse save(RiskResponseTreatmentDto request) throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		
		Risk risk = riskRepository.findById(request.getRiskId())
		.filter(r -> !r.getDeleted())
		.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));
		
		User riskReporting = userRepository.findById(request.getRiskReporting())
		.filter(u -> !u.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("User not found for selected risk reporting."));
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(request.getRiskResponseTreatmentId())
		.filter(r -> !r.getDeleted())
		.orElse(new RiskResponseTreatment());
		
		List<SubRisk> subRisks = risk.getSubRisk().stream().filter(r-> request.getRiskSubIds().contains(r.getId()))
				.map(sbRisk->{
					sbRisk.setRiskResponseTreatment(riskResponseTreatment);
					return sbRisk;
				})
				 .collect(Collectors.toList());
		
		riskResponseTreatment.setOrganization(organization);
		riskResponseTreatment.setCompany(company);
		
		riskResponseTreatment.setSubRisks(subRisks);
		riskResponseTreatment.setRisk(risk);
		riskResponseTreatment.setControlPresence(request.getControlPresence());
		riskResponseTreatment.setControlDescription(request.getControlDescription());
		riskResponseTreatment.setControlGapsIdentified(request.getControlGapsIdentified());
		riskResponseTreatment.setRecommendedControl(request.getRecommendedControl());
		riskResponseTreatment.setManagementActionPlan(request.getManagementActionPlan());
		riskResponseTreatment.setContingencyPlans(request.getContingencyPlans());
		riskResponseTreatment.setControlEffectivenessPercentage(request.getControlEffectiveness());
		riskResponseTreatment.setControlEffectivenessWeightage(request.getControlEffectivenessWeightage());
		riskResponseTreatment.setControlEvaluationStatus(request.getControlEvaluationStatus());
		riskResponseTreatment.setRiskTreatmentStatus(request.getRiskTreatmentStatus());
		riskResponseTreatment.setEvidenceRequire(request.getEvidenceRequire());
		riskResponseTreatment.setSupportingEvidence(request.getSupportingEvidence());
		riskResponseTreatment.setControlEvaluationBy(request.getControlEvaluationBy());
		riskResponseTreatment.setRiskReporting(riskReporting);
		riskResponseTreatment.setControlStatus(request.getControlStatus());
		
		
		RiskResponseTreatment saved = riskResponseTreatmentRepository.save(riskResponseTreatment);
		return new RiskResponseTreatmentResponse(saved);
	}
	@Override
	public RiskResponseTreatmentResponse getRiskTreatment(Long treatmentId) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();
		
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.getOrgRiskResponseTreatment(organization.getId(), treatmentId);
		
	return new RiskResponseTreatmentResponse(riskResponseTreatment);
	}
	@Override
	public List<CustomResponse> getRiskTreatmentView(Long id) throws ResourceNotFoundException{
		RiskResponseTreatmentResponse riskTreatment = getRiskTreatment(id);
		List<CustomResponse> response = new ArrayList<>();
		List<CustomFieldResponse> customFieldResponse = fieldService.getCustomFieldResponse(1, "riskTreatment");
		
		customFieldResponse.stream()
		.forEach(customField->{
			CustomResponse customResponse;
			try {
				customResponse = CustomResponseMapperUtil.map(riskTreatment, customField, "riskTreatment");
				if(customResponse != null) {
					response.add(customResponse);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error("getRiskTreatmentView() {}", e.getMessage());
			}

			
		});
		
		
		return response;
	}
	@Override
	public List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		List<RiskResponseTreatment> riskResponseTreatment = riskResponseTreatmentRepository.getAllOrgRiskResponseTreatments(organization.getId());

		List<List<CustomResponse>> responseList = new ArrayList<>();
		 
		 for(RiskResponseTreatment riskTreatment : riskResponseTreatment) {
			 List<CustomResponse> response = customResponseMapper.map("riskTreatment", 1l, new RiskResponseTreatmentResponse(riskTreatment), true);
		 
			 responseList.add(response);
		 }
		 
		 
		 return responseList;
	}

	@Override
	public Page<List<CustomResponse>> getAllRisks(Pageable pageable) {
	    Organization organization = OrganizationContext.getOrganization();

	    Page<RiskResponseTreatment> riskPage =
	            riskResponseTreatmentRepository.getAllOrgRiskResponseTreatments(
	                    organization.getId(), pageable
	            );

	    List<List<CustomResponse>> responseList = riskPage.getContent().stream()
	            .map(this::mapRiskTreatment)
	            .collect(Collectors.toList());

	    return new PageImpl<>(responseList, pageable, riskPage.getTotalElements());
	}
	
	
	private List<CustomResponse> mapRiskTreatment(RiskResponseTreatment riskTreatment) {
	    try {
	        return customResponseMapper.map(
	                "riskTreatment",
	                1L,
	                new RiskResponseTreatmentResponse(riskTreatment),
	                true
	        );
	    } catch (Exception e) {
	        throw new RuntimeException(
	                "Error mapping RiskResponseTreatment with id: " + riskTreatment.getId(), e
	        );
	    }
	}
	
}
