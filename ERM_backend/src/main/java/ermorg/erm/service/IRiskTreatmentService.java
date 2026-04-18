package ermorg.erm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
