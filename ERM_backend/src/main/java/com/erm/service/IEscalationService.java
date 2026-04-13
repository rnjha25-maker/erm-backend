package com.erm.service;

import java.util.List;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.EscalationResponse;
import com.erm.dto.riskDTO.EscalationRequestDto;
import com.erm.exception.ResourceNotFoundException;

public interface IEscalationService {

	public EscalationResponse save(EscalationRequestDto request) throws ResourceNotFoundException;

	public EscalationResponse get(Long esclationId) throws ResourceNotFoundException;

	List<CustomResponse> getById(Long esclationId) throws ResourceNotFoundException;

	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException;

	void delete(Long esclationId) throws ResourceNotFoundException;
}
