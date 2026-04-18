package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IKriKpiRiskService {

	public KriKpiReviewResponseDTO save(KriKpiReviewRequestDTO request) throws ResourceNotFoundException;

	public KriKpiReviewResponseDTO get(Long kriId) throws ResourceNotFoundException;

	List<CustomResponse> getView(Long kriId) throws ResourceNotFoundException;
	
	List<List<CustomResponse>> getAll() throws ResourceNotFoundException;

	void delete(Long kriId) throws ResourceNotFoundException;
}
