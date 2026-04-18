package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IRiskTreatmentService;

@RestController
@RequestMapping("/risk-treatment")
public class RiskTreatmentController {
	
	@Autowired
	private IRiskTreatmentService riskTreatmentService;
	
	@PostMapping("/save")
	public GeneralResponse<RiskResponseTreatmentResponse> save(@RequestBody RiskResponseTreatmentDto riskResponseTreatmentDto) throws ResourceNotFoundException {
		GeneralResponse<RiskResponseTreatmentResponse> response = new GeneralResponse<>();
		RiskResponseTreatmentResponse savedData = riskTreatmentService.save(riskResponseTreatmentDto);
		
		response.setData(savedData);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@GetMapping("/{id}")
	public GeneralResponse<RiskResponseTreatmentResponse> getRiskTreatment(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<RiskResponseTreatmentResponse> response = new GeneralResponse<>();
		RiskResponseTreatmentResponse data = riskTreatmentService.getRiskTreatment(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@GetMapping("get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskTreatmentView(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();
		List<CustomResponse> data = riskTreatmentService.getRiskTreatmentView(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@GetMapping("/all")
	public GeneralResponse<List<List<CustomResponse>>> getAllRisks() throws ResourceNotFoundException{
		
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();
		
			List<List<CustomResponse>> risks = riskTreatmentService.getAllRisks();
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

		Page<List<CustomResponse>> risks = riskTreatmentService.getAllRisks(pageable);
		response.setData(risks);
		response.setMessage("Risks fetched with pagination.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

}
