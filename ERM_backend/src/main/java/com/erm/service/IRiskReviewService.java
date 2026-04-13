package com.erm.service;

import java.util.List;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.RiskReviewResponseDtoResponse;
import com.erm.dto.riskDTO.RiskReviewRequestDTO;
import com.erm.exception.ResourceNotFoundException;

public interface IRiskReviewService {

    public RiskReviewResponseDtoResponse saveRiskReview(RiskReviewRequestDTO requestDTO) throws ResourceNotFoundException;
	public RiskReviewResponseDtoResponse get(Long id) throws ResourceNotFoundException;
	public List<CustomResponse> getView(Long id) throws ResourceNotFoundException;
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException;
	public void delete(Long id) throws ResourceNotFoundException;

}
