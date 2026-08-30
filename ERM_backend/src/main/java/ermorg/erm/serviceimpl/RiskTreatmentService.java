package ermorg.erm.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskResponseTreatment;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.User;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskResponseTreatmentRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.IFieldService;
import ermorg.erm.service.IRiskTreatmentService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.SubRiskSelectionUtil;
import ermorg.erm.util.mapper.CustomResponseMapper;
import ermorg.erm.util.mapper.CustomResponseMapperUtil;
import ermorg.storage.dto.response.DocumentDto;
import ermorg.storage.exception.InvalidResourceAccess;
import ermorg.storage.service.DocumentStorageService;
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
	private IFieldService fieldService;

	@Autowired
	private DocumentStorageService documentStorageService;
	@Override
	public RiskResponseTreatmentResponse save(RiskResponseTreatmentDto request) throws ResourceNotFoundException {
		
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		validateRequiredId(request.getRiskId(), "Please select risk.");
		validateRequiredId(request.getRiskReporting(), "Please select risk reporting.");
		
		Risk risk = java.util.Optional.ofNullable(riskRepository.getRisksByOrgIdAndRiskId(organization.getId(), request.getRiskId()))
		.orElseThrow(() -> new ResourceNotFoundException("Risk not found."));
		
		User riskReporting = userRepository.findById(request.getRiskReporting())
		.filter(u -> !u.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("User not found for selected risk reporting."));
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(request.getRiskResponseTreatmentId())
		.filter(r -> !r.getDeleted())
		.orElse(new RiskResponseTreatment());
		
		List<SubRisk> subRisks = SubRiskSelectionUtil.resolveSelectedSubRisks(risk, request.getRiskSubIds()).stream()
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
		riskResponseTreatment.setRiskAppetiteStatus(request.getRiskAppetiteStatus());
		riskResponseTreatment.setRiskAcceptanceLevel(request.getRiskAcceptanceLevel());
		riskResponseTreatment.setEvidenceRequire(request.getEvidenceRequire());
		riskResponseTreatment.setSupportingEvidence(request.getSupportingEvidence());
		riskResponseTreatment.setSupportingEvidenceDocument(request.getSupportingEvidenceDocument());
		riskResponseTreatment.setControlEvaluationBy(request.getControlEvaluationBy());
		riskResponseTreatment.setRiskReporting(riskReporting);
		riskResponseTreatment.setControlStatus(request.getControlStatus());
		
		
		RiskResponseTreatment saved = riskResponseTreatmentRepository.save(riskResponseTreatment);
		return new RiskResponseTreatmentResponse(saved);
	}

	private void validateRequiredId(long id, String message) throws ResourceNotFoundException {
		if (id <= 0) {
			throw new ResourceNotFoundException(message);
		}
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
		return customResponseMapper.map("riskTreatment", 1L, riskTreatment, false);
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
	
	public RiskResponseTreatmentResponse uploadEvidence(Long riskTreatmentId, MultipartFile file, String description,
			String purpose) throws IOException, InvalidResourceAccess, ResourceNotFoundException {
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(riskTreatmentId)
				.filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk response treatment not found."));
		Long organizationId = riskResponseTreatment.getOrganization() != null ? riskResponseTreatment.getOrganization().getId() : null;
		Long companyId = riskResponseTreatment.getCompany() != null ? riskResponseTreatment.getCompany().getId() : null;
		DocumentDto document = documentStorageService.uploadDocument(file, organizationId, companyId, purpose);
		if (description != null && !description.isBlank()) {
			riskResponseTreatment.setSupportingEvidence(description);
		}
		riskResponseTreatment.setSupportingEvidenceDocument(UUID.fromString(document.getDocumentId()));
		riskResponseTreatmentRepository.save(riskResponseTreatment);
		return new RiskResponseTreatmentResponse(riskResponseTreatment);
	}
	
	public Object getEvidence(Long riskTreatmentId) throws ResourceNotFoundException {
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(riskTreatmentId)
				.filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk response treatment not found."));
		UUID evidenceDocumentId = riskResponseTreatment.getSupportingEvidenceDocument();
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		response.put("documentId", evidenceDocumentId != null ? evidenceDocumentId.toString() : null);
		response.put("description", riskResponseTreatment.getSupportingEvidence());
		if (evidenceDocumentId == null) {
			return response;
		}
		DocumentDto document;
		try {
			document = documentStorageService.getDocument(evidenceDocumentId.toString());
		} catch (ermorg.storage.exception.ResourceNotFoundException ex) {
			throw new ResourceNotFoundException("Supporting evidence document not found.");
		}
		response.put("fileName", document.getFileName() + document.getFileExtension());
		response.put("contentType", document.getContentType());
		response.put("purpose", document.getPurpose());
		response.put("path", document.getFilePath());
		return response;
	}
	
	public Object downloadEvidence(String documentId) throws IOException, ermorg.storage.exception.ResourceNotFoundException {
		return documentStorageService.downloadDocument(documentId);
	}
	
	public void deleteEvidence(Long riskTreatmentId, String documentId) throws ResourceNotFoundException, ermorg.storage.exception.ResourceNotFoundException {
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(riskTreatmentId)
				.filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk response treatment not found."));
		if (riskResponseTreatment.getSupportingEvidenceDocument() != null
				&& riskResponseTreatment.getSupportingEvidenceDocument().toString().equalsIgnoreCase(documentId)) {
			riskResponseTreatment.setSupportingEvidenceDocument(null);
			riskResponseTreatment.setSupportingEvidence(null);
			riskResponseTreatmentRepository.save(riskResponseTreatment);
		}
		documentStorageService.deleteDocument(documentId);
	}
	
	private List<CustomResponse> mapRiskTreatment(RiskResponseTreatment riskTreatment) {
	    return customResponseMapper.map(
		        "riskTreatment",
		        1L,
		        new RiskResponseTreatmentResponse(riskTreatment),
		        true
		);
	}
	
}
