package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.riskDTO.ErmMaturityRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;

public interface IErmMaturityService {

	public ErmMaturityResponse save(ErmMaturityRequest request) throws ResourceNotFoundException;

	public ErmMaturityResponse get(Long maturityId)throws ResourceNotFoundException;

	void delete(Long maturityId) throws ResourceNotFoundException;

	List<List<CustomResponse>> getAll(Pageable pagable) throws ResourceNotFoundException;

	List<CustomResponse> getView(Long maturityId) throws ResourceNotFoundException;
}
