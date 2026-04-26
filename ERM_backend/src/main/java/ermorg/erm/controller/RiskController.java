package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.AllRiskDropdownResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.riskDTO.RiskAsessmentDto;
import ermorg.erm.dto.riskDTO.RiskDTO;
import ermorg.erm.dto.riskDTO.UpdateRiskStatusRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Risk;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IRiskService;

@RestController
@RequestMapping("risk")
public class RiskController {

	@Autowired
	private IRiskService riskService;

	@PostMapping("/save")
	public GeneralResponse<RiskResponse> addRisk(@RequestBody RiskDTO request) {
		GeneralResponse<RiskResponse> response = new GeneralResponse<>();
		try {
			RiskResponse risk = riskService.addRisk(request);

			response.setData(risk);
			response.setMessage("Risk Added Successfully");
			response.setStatus(ResponseStatus.SUCCESS);
		} catch (Exception e) {
			response.setMessage("Could not add Risk");
			e.printStackTrace();
			System.out.println(e.getMessage());
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	@PostMapping("/save-assessment")
	public GeneralResponse<RiskResponse> saveRiskAssessment(@RequestBody RiskAsessmentDto request) {
		GeneralResponse<RiskResponse> response = new GeneralResponse<>();
		try {
			RiskResponse risk = riskService.addRiskAssessment(request);

			response.setData(risk);
			response.setMessage("Risk Added Successfully");
			response.setStatus(ResponseStatus.SUCCESS);
		} catch (Exception e) {
			response.setMessage("Could not add Risk");
			e.printStackTrace();
			System.out.println(e.getMessage());
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	@GetMapping("/get-risk/{id}")
	public GeneralResponse<RiskResponse> getRisk(@PathVariable("id") Long id) {
		GeneralResponse<RiskResponse> response = new GeneralResponse<>();

		try {
			RiskResponse risk = riskService.getRisk(id);
			if (risk == null) {
				response.setMessage("No Risk found.");
			} else {
				response.setData(risk);
				response.setMessage("Risk fetched.");
			}

			response.setStatus(ResponseStatus.SUCCESS);
		} catch (Exception e) {
			response.setMessage("Failed to fetch.");
			response.setStatus(ResponseStatus.FAILED);
			System.out.println(e.getMessage());
		}

		return response;
	}

	@DeleteMapping("/delete-risk/{id}")
	public GeneralResponse<Risk> deleteRisk(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<Risk> response = new GeneralResponse<>();

		riskService.deleteRisk(id);
		response.setMessage("Risk deleted.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@GetMapping("/all-risks")
	public GeneralResponse<Page<CustomResponse>> getAllRisksPaginated(
			@org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
			throws ResourceNotFoundException {

		Page<CustomResponse> risks = riskService.getAllRisks(pageable);

		GeneralResponse<Page<CustomResponse>> response = new GeneralResponse<>();
		response.setData(risks);
		response.setMessage("Risks fetched with pagination.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@GetMapping("/risks/dropdown")
	public GeneralResponse<Page<AllRiskDropdownResponse>> getAllRiskDropdown(
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
			throws ResourceNotFoundException {

		Page<AllRiskDropdownResponse> risks = riskService.getAllRiskDropdown(pageable);

		GeneralResponse<Page<AllRiskDropdownResponse>> response = new GeneralResponse<>();
		response.setData(risks);
		response.setMessage("Risks fetched successfully.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@GetMapping("/all-assessments")
	public GeneralResponse<Page<CustomResponse>> getAllAssessments(

			@org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
			throws ResourceNotFoundException {

		Page<CustomResponse> risks = riskService.getAllAssessment(pageable);

		GeneralResponse<Page<CustomResponse>> response = new GeneralResponse<>();
		response.setData(risks);
		response.setMessage("Assessments fetched successfully.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@GetMapping("/get-risk-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskView(@PathVariable("id") Long id)
			throws ResourceNotFoundException {

		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<List<CustomResponse>>();
		List<CustomResponse> data = riskService.getRiskView(id);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/get-risk-assessment-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskAssessmentView(@PathVariable("id") Long id)
			throws ResourceNotFoundException {

		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<List<CustomResponse>>();
		List<CustomResponse> data = riskService.getRisAssessmentkView(id);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@PutMapping("/update-status")
	public GeneralResponse<RiskResponse> updateRiskStatus(@RequestBody UpdateRiskStatusRequest request) {
		GeneralResponse<RiskResponse> response = new GeneralResponse<>();
		try {
			RiskResponse risk = riskService.updateRiskStatus(request);

			response.setData(risk);
			response.setMessage("Risk status updated successfully");
			response.setStatus(ResponseStatus.SUCCESS);
		} catch (ResourceNotFoundException e) {
			response.setMessage(e.getMessage());
			response.setStatus(ResponseStatus.FAILED);
		} catch (Exception e) {
			response.setMessage("Could not update risk status");
			e.printStackTrace();
			System.out.println(e.getMessage());
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

}
