package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.dto.riskDTO.RiskReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IRiskReviewService {

    public RiskReviewResponseDtoResponse saveRiskReview(RiskReviewRequestDTO requestDTO) throws ResourceNotFoundException;
	public RiskReviewResponseDtoResponse get(Long id) throws ResourceNotFoundException;
	public List<CustomResponse> getView(Long id) throws ResourceNotFoundException;
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException;
	public void delete(Long id) throws ResourceNotFoundException;

}
