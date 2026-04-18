package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.riskDTO.RiskControlDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Risk;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IRiskControlService;

@RestController
@RequestMapping("/riskcontrol")
public class RiskControlController {

	@Autowired
	private IRiskControlService riskControlService;
	
	@PostMapping("/save")
	public GeneralResponse<RiskControlResponse> saveRiskControl(@RequestBody RiskControlDto request) throws ResourceNotFoundException{
		
		GeneralResponse<RiskControlResponse> response = new GeneralResponse<RiskControlResponse>();
		
		RiskControlResponse saved = riskControlService.saveRiskControl(request);
		
		response.setData(saved);
		response.setMessage("Risk Control saved.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@PostMapping("/{id}")
	public GeneralResponse<RiskControlResponse> getRiskControl(@PathVariable Long id) throws ResourceNotFoundException{
		
		GeneralResponse<RiskControlResponse> response = new GeneralResponse<RiskControlResponse>();
		
		RiskControlResponse data = riskControlService.getRiskControl(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@DeleteMapping("/delete-risk/{id}")
	public GeneralResponse<RiskControlResponse> deleteRisk(@PathVariable("id") Long id) {
		GeneralResponse<RiskControlResponse> response = new GeneralResponse<>();

		try {
			riskControlService.deleteRisk(id);
			response.setMessage("Risk deleted.");
			response.setStatus(ResponseStatus.SUCCESS);
		} catch (Exception e) {
			response.setMessage("Failed to delete.");
			response.setStatus(ResponseStatus.FAILED);
			System.out.println(e.getMessage());
		}

		return response;
	}
	
	@GetMapping("/all")
	public GeneralResponse<List<List<CustomResponse>>> getAllRisks() throws ResourceNotFoundException{
		
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();
		
			List<List<CustomResponse>> risks = riskControlService.getAllRisks();
			response.setData(risks);
			response.setMessage("Risks fetched.");
			response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}

	@GetMapping("/all-paginated")
	public GeneralResponse<Page<List<CustomResponse>>> getAllRisksPaginated(
			@org.springframework.data.web.PageableDefault(size = 20) Pageable pageable)
			throws ResourceNotFoundException {

		GeneralResponse<Page<List<CustomResponse>>> response = new GeneralResponse<>();

		Page<List<CustomResponse>> risks = riskControlService.getAllRisks(pageable);
		response.setData(risks);
		response.setMessage("Risks fetched with pagination.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}
	
	@GetMapping("/get-risk-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskView(@PathVariable("id") Long id) throws ResourceNotFoundException{ 
		
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<List<CustomResponse>>();
		List<CustomResponse> data = riskControlService.getRiskView(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
}
