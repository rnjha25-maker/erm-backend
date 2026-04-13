package com.erm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.RiskControlResponse;
import com.erm.dto.riskDTO.RiskControlDto;
import com.erm.exception.ResourceNotFoundException;
 
public interface IRiskControlService {



	Page<List<CustomResponse>> getAllRisks(Pageable pageable) throws ResourceNotFoundException;

	RiskControlResponse saveRiskControl(RiskControlDto request) throws ResourceNotFoundException;

	RiskControlResponse getRiskControl(Long id);

	void deleteRisk(Long id);

	List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;

	List<CustomResponse> getRiskView(Long id) throws ResourceNotFoundException;

}
