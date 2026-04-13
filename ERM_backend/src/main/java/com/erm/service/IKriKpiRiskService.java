package com.erm.service;

import java.util.List;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.KriKpiReviewResponseDTO;
import com.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import com.erm.exception.ResourceNotFoundException;

public interface IKriKpiRiskService {

	public KriKpiReviewResponseDTO save(KriKpiReviewRequestDTO request) throws ResourceNotFoundException;

	public KriKpiReviewResponseDTO get(Long kriId) throws ResourceNotFoundException;

	List<CustomResponse> getView(Long kriId) throws ResourceNotFoundException;
	
	List<List<CustomResponse>> getAll() throws ResourceNotFoundException;

	void delete(Long kriId) throws ResourceNotFoundException;
}
