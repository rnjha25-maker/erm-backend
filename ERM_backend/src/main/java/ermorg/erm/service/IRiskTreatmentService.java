package ermorg.erm.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IRiskTreatmentService {

	public RiskResponseTreatmentResponse save(RiskResponseTreatmentDto request) throws ResourceNotFoundException;

	public RiskResponseTreatmentResponse getRiskTreatment(Long id) throws ResourceNotFoundException;

	public List<CustomResponse> getRiskTreatmentView(Long id) throws ResourceNotFoundException;

	public List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;

	public Page<List<CustomResponse>> getAllRisks(Pageable pageable) throws ResourceNotFoundException;

	public RiskResponseTreatmentResponse uploadEvidence(Long riskTreatmentId, MultipartFile file, String description,
			String purpose) throws IOException, ResourceNotFoundException;

	public Object getEvidence(Long riskTreatmentId) throws ResourceNotFoundException;

	public Object downloadEvidence(String documentId) throws IOException, ResourceNotFoundException;

	public void deleteEvidence(Long riskTreatmentId, String documentId) throws ResourceNotFoundException;
}
