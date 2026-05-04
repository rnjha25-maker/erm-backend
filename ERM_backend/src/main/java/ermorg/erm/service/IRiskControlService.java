package ermorg.erm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.riskDTO.RiskControlDto;
import ermorg.erm.exception.ResourceNotFoundException;
 
public interface IRiskControlService {



	Page<List<CustomResponse>> getAllRisks(Pageable pageable) throws ResourceNotFoundException;

	RiskControlResponse saveRiskControl(RiskControlDto request) throws ResourceNotFoundException;

	RiskControlResponse getRiskControl(Long id);

	void deleteRisk(Long id);

	List<List<CustomResponse>> getAllRisks() throws ResourceNotFoundException;

	List<CustomResponse> getRiskView(Long id) throws ResourceNotFoundException;

}
