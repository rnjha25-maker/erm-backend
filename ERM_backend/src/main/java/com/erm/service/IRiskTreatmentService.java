package com.erm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.RiskResponseTreatmentResponse;
import com.erm.dto.riskDTO.RiskResponseTreatmentDto;
import com.erm.exception.ResourceNotFoundException;

public interface IRiskTreatmentService {

	public RiskResponseTreatmentResponse save(RiskResponseTreatmentDto request) throws ResourceNotFoundException;

	public RiskResponseTreatmentResponse getRiskTreatment(Long id) throws ResourceNotFoundException;

	public List<CustomResponse> getRiskTreatmentView(Long id) throws ResourceNotFoundException;

	public List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;

	public Page<List<CustomResponse>> getAllRisks(Pageable pageable) throws ResourceNotFoundException;
}
