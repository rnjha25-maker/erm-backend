package ermorg.erm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.riskDTO.RiskControlDto;
import ermorg.erm.dto.riskDTO.RiskSubControlDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskControl;
import ermorg.erm.model.RiskSubControl;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.RiskControlRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.SubRiskControlRepository;
import ermorg.erm.repository.SubRiskRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;
import ermorg.erm.util.mapper.CustomResponseMapperUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RiskControlService implements IRiskControlService {

	@Autowired
	private RiskControlRepository riskControlRepository;
	
	@Autowired
	private RiskRepository riskRepository;
	
	@Autowired
	private SubRiskRepository subRiskRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SubRiskControlRepository subRiskControlRepository;
	
	@Autowired
	private IFieldService fieldService;
	
	@Autowired
	private CustomResponseMapper customResponseMapper;
	
	@Override
	public RiskControlResponse saveRiskControl(RiskControlDto request)
			throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		 RiskControl riskControl = riskControlRepository.findById(request.getRiskControlId())
		.filter(d-> !d.getDeleted())
		.orElse(new RiskControl());

		 Risk risk = riskRepository.findById(request.getRiskId())
		 .filter(d-> !d.getDeleted())
		 .orElseThrow(()-> new ResourceNotFoundException("Risk not found for selected risk."));
		 
		 User approver = userRepository.findById(request.getApprover())
		 .filter(d -> !d.getDeleted())
		 .orElseThrow(()-> new ResourceNotFoundException("User not found for selected approver."));
		 
		 User primaryResponsible = userRepository.findById(request.getPrimaryResponsible())
		 .filter(d -> !d.getDeleted())
		 .orElseThrow(()-> new ResourceNotFoundException("User not found for selected primary responsible."));
		 
		 List<SubRisk> subRisks = risk.getSubRisk().stream().filter(r-> request.getSubRiskIds().contains(r.getId()))
		 .collect(Collectors.toList());
		 
		 riskControl.setOrganization(organization);
		 riskControl.setCompany(company);
		 riskControl.setSubRisk(subRisks);
		 riskControl.setRisk(risk);
		 riskControl.setControlSource(request.getControlSource());
		 riskControl.setControlTitle(request.getControlTitle());
		 riskControl.setControlType(request.getControlType());
		 riskControl.setControlMechanism(request.getControlMechanism());
		 riskControl.setAccessControls(request.getAccessControls());
		 riskControl.setProcessControls(request.getProcessControls());
		 riskControl.setPhysicalControls(request.getPhysicalControls());
		 riskControl.setTechnicalControl(request.getTechnicalControl());
		 riskControl.setControlAssessmentFrequency(request.getControlAssessmentFrequency());
		 riskControl.setPrimaryResponsible(primaryResponsible);
		 riskControl.setApprover(approver);
		 riskControl.setActualDate(request.getActualDate());
		 riskControl.setLastControlAssessmentDate(request.getLastControlAssessmentDate());
		 riskControl.setNextControlAssessmentDate(request.getNextControlAssessmentDate());
		 riskControl.setDeleted(false);
		
		 
			List<RiskSubControl> riskSubControlList = new ArrayList<>(); 
		 for (RiskSubControlDto subControlDto : request.getControlSubTitle()) {
				RiskSubControl  riskSubControl = new RiskSubControl();
				if (subControlDto.getSubControlId() != 0) {
					riskSubControl = subRiskControlRepository.findById(subControlDto.getSubControlId()).orElse(new RiskSubControl());
				}

				riskSubControl.setSubControlTitle(subControlDto.getControlSubTitle());
				riskSubControl.setRiskControl(riskControl);
				riskSubControlList.add(riskSubControl);

			}
		 
		 RiskControl savedControl = riskControlRepository.save(riskControl);
		
		return new RiskControlResponse(savedControl);
	}

	@Override
	public RiskControlResponse getRiskControl(Long id) {
		Organization organization = OrganizationContext.getOrganization();

		RiskControl riskControl = riskControlRepository.getOrObject(organization.getId(), id);
		return new RiskControlResponse(riskControl);
	}

	@Override
	public void deleteRisk(Long id) {
		Organization organization = OrganizationContext.getOrganization();

		RiskControl riskControl = riskControlRepository.getOrObject(organization.getId(), id);
		riskControl.setDeleted(true);
		riskControlRepository.save(riskControl);
		
	}

	@Override
	public List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		List<RiskControl> riskList = riskControlRepository.getAllRisksByOrganizationIdNoPage(organization.getId());
		List<List<CustomResponse>> responseList = new ArrayList<>();
		
		for(RiskControl risk : riskList) {
			List<CustomResponse> response = customResponseMapper.map("riskControl", 1L, new RiskControlResponse(risk), true);
			responseList.add(response);
		}
		
		return responseList;
	}

	@Override
	public Page<List<CustomResponse>> getAllRisks(Pageable pageable) throws ResourceNotFoundException {
	    Organization organization = OrganizationContext.getOrganization();

	    Page<RiskControl> riskPage = riskControlRepository.getAllRisksByOrganizationId(organization.getId(), pageable);

	    List<List<CustomResponse>> responseList = new ArrayList<>();
	    for (RiskControl risk : riskPage.getContent()) {
	        responseList.add(customResponseMapper.map("riskControl", 1L, new RiskControlResponse(risk), true));
	    }

	    return new PageImpl<>(responseList, pageable, riskPage.getTotalElements());
	}

	@Override
	public List<CustomResponse> getRiskView(Long riskId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) {
			throw new ResourceNotFoundException("Organization not found");
		}

		List<CustomResponse> customResponses = new ArrayList<>();
		RiskControl risk = riskControlRepository.getRisksByOrgIdAndRiskId(organization.getId(), riskId);
		List<CustomFieldResponse> customFieldResponse = fieldService.getCustomFieldResponse(1, "riskControl");
		
		customFieldResponse.stream().forEach(field -> {
			try {
				CustomResponse customResponse = CustomResponseMapperUtil.map(risk, field, "riskControl");
				customResponses.add(customResponse);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		});
		return customResponses;
	}
	
	
	
	
	
	

}
