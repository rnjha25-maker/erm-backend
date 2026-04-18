package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.EscalationResponse;
import ermorg.erm.dto.riskDTO.EscalationRequestDto;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IEscalationService {

	public EscalationResponse save(EscalationRequestDto request) throws ResourceNotFoundException;

	public EscalationResponse get(Long esclationId) throws ResourceNotFoundException;

	List<CustomResponse> getById(Long esclationId) throws ResourceNotFoundException;

	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException;

	void delete(Long esclationId) throws ResourceNotFoundException;
}
