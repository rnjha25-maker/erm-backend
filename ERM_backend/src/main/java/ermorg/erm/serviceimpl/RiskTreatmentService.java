package ermorg.erm.serviceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
	private RestTemplate restTemplate;

	@Value("${erm.storage-service-url:http://storage}")
	private String storageServiceUrl;

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
			String purpose) throws IOException, ResourceNotFoundException {
		if (file == null || file.isEmpty()) {
			throw new ResourceNotFoundException("Please select a file to upload.");
		}
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(riskTreatmentId)
				.filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk response treatment not found."));
		Map<String, Object> document = uploadDocument(file, purpose);
		if (description != null && !description.isBlank()) {
			riskResponseTreatment.setSupportingEvidence(description);
		}
		riskResponseTreatment.setSupportingEvidenceDocument(UUID.fromString(requiredDocumentId(document)));
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
		Map<String, Object> document = downloadDocument(evidenceDocumentId.toString());
		response.put("fileName", stringValue(document.get("fileName")) + stringValue(document.get("fileExtension")));
		response.put("contentType", document.get("contentType"));
		response.put("purpose", document.get("purpose"));
		response.put("path", document.get("filePath"));
		return response;
	}
	
	public Object downloadEvidence(String documentId) throws IOException, ResourceNotFoundException {
		return downloadDocument(documentId);
	}
	
	public void deleteEvidence(Long riskTreatmentId, String documentId) throws ResourceNotFoundException {
		RiskResponseTreatment riskResponseTreatment = riskResponseTreatmentRepository.findById(riskTreatmentId)
				.filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
				.orElseThrow(() -> new ResourceNotFoundException("Risk response treatment not found."));
		if (riskResponseTreatment.getSupportingEvidenceDocument() != null
				&& riskResponseTreatment.getSupportingEvidenceDocument().toString().equalsIgnoreCase(documentId)) {
			riskResponseTreatment.setSupportingEvidenceDocument(null);
			riskResponseTreatment.setSupportingEvidence(null);
			riskResponseTreatmentRepository.save(riskResponseTreatment);
		}
		deleteDocument(documentId);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> uploadDocument(MultipartFile file, String purpose) throws IOException, ResourceNotFoundException {
		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
		String extension = StringUtils.getFilenameExtension(originalFilename);
		String fileName = StringUtils.stripFilenameExtension(originalFilename);

		Map<String, Object> request = new HashMap<>();
		request.put("fileName", fileName == null || fileName.isBlank() ? "supporting-evidence" : fileName);
		request.put("fileExtension", "." + (extension == null || extension.isBlank() ? "bin" : extension));
		request.put("contentType", file.getContentType() == null ? "application/octet-stream" : file.getContentType());
		request.put("purpose", purpose == null || purpose.isBlank() ? "risk-response-treatment" : purpose);
		request.put("fileContent", Base64.getEncoder().encodeToString(file.getBytes()));

		try {
			Map<String, Object> response = restTemplate.postForObject(storageUrl("/upload"), request, Map.class);
			return extractData(response);
		} catch (RestClientException ex) {
			log.error("Supporting evidence upload failed.", ex);
			throw new ResourceNotFoundException("Supporting evidence upload failed.");
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> downloadDocument(String documentId) throws ResourceNotFoundException {
		try {
			Map<String, Object> response = restTemplate.getForObject(storageUrl("/download/" + documentId), Map.class);
			return extractData(response);
		} catch (RestClientException ex) {
			log.error("Supporting evidence fetch failed for documentId: {}", documentId, ex);
			throw new ResourceNotFoundException("Supporting evidence document not found.");
		}
	}

	private void deleteDocument(String documentId) throws ResourceNotFoundException {
		try {
			restTemplate.delete(storageUrl("/delete/" + documentId));
		} catch (RestClientException ex) {
			log.error("Supporting evidence delete failed for documentId: {}", documentId, ex);
			throw new ResourceNotFoundException("Supporting evidence document not found.");
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractData(Map<String, Object> response) throws ResourceNotFoundException {
		Object data = response == null ? null : response.get("data");
		if (data instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		throw new ResourceNotFoundException("Invalid response from storage service.");
	}

	private String requiredDocumentId(Map<String, Object> document) throws ResourceNotFoundException {
		String documentId = stringValue(document.get("documentId"));
		if (documentId.isBlank()) {
			throw new ResourceNotFoundException("Storage service did not return a document id.");
		}
		return documentId;
	}

	private String storageUrl(String path) {
		return storageServiceUrl.replaceAll("/+$", "") + path;
	}

	private String stringValue(Object value) {
		return value == null ? "" : value.toString();
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
