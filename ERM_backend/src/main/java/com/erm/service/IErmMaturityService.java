package com.erm.service;

import java.util.List;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.ErmMaturityResponse;
import com.erm.dto.riskDTO.ErmMaturityRequest;
import com.erm.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;

public interface IErmMaturityService {

	public ErmMaturityResponse save(ErmMaturityRequest request) throws ResourceNotFoundException;

	public ErmMaturityResponse get(Long maturityId)throws ResourceNotFoundException;

	void delete(Long maturityId) throws ResourceNotFoundException;

	List<List<CustomResponse>> getAll(Pageable pagable) throws ResourceNotFoundException;

	List<CustomResponse> getView(Long maturityId) throws ResourceNotFoundException;
}
