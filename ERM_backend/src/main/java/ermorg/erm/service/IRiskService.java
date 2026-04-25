package ermorg.erm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ermorg.erm.dto.response.AllRiskDropdownResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.riskDTO.RiskAsessmentDto;
import ermorg.erm.dto.riskDTO.RiskDTO;
import ermorg.erm.dto.riskDTO.UpdateRiskStatusRequest;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IRiskService {
	/*
	 * RiskResponse addRisk(RiskDTO request) throws ResourceNotFoundException;
	 * RiskResponse getRisk(Long id) throws ResourceNotFoundException; void
	 * deleteRisk(Long id) throws ResourceNotFoundException;
	 * List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;
	 */
	Page<CustomResponse> getAllRisks(Pageable pageable) throws ResourceNotFoundException;
	RiskResponse addRisk(RiskDTO request) throws ResourceNotFoundException;
	RiskResponse getRisk(Long id) throws ResourceNotFoundException;
	void deleteRisk(Long id) throws ResourceNotFoundException;
	//List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;
	RiskResponse addRiskAssessment(RiskAsessmentDto request) throws ResourceNotFoundException;
	List<CustomResponse> getRiskView(Long riskId) throws ResourceNotFoundException;
	List<CustomResponse> getRisAssessmentkView(Long assessmentId) throws ResourceNotFoundException;
	Page<CustomResponse> getAllAssessment(Pageable pageable) throws ResourceNotFoundException;
	Page<AllRiskDropdownResponse> getAllRiskDropdown(Pageable pageable)throws ResourceNotFoundException;
	RiskResponse updateRiskStatus(UpdateRiskStatusRequest request) throws ResourceNotFoundException;
}
