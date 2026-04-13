package com.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.dto.ResponseStatus;
import com.erm.dto.response.AllRiskDropdownResponse;
import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.RiskResponse;
import com.erm.dto.riskDTO.RiskAsessmentDto;
import com.erm.dto.riskDTO.RiskDTO;
import com.erm.dto.riskDTO.UpdateRiskStatusRequest;
import com.erm.exception.ResourceNotFoundException;
import com.erm.model.Risk;
import com.erm.response.GeneralResponse;
import com.erm.service.IRiskService;

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
			if(risk == null) {
				response.setMessage("No Risk found.");
			}else {
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
	public GeneralResponse<List<List<CustomResponse>>> getAllRisks() throws ResourceNotFoundException{
		
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
			Page<List<CustomResponse>> risks = riskService.getAllRisks(pageable);
			response.setData(risks.getContent());
			response.setMessage("Risks fetched.");
			response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}

	@GetMapping("/all-risks-paginated")
	public GeneralResponse<Page<List<CustomResponse>>> getAllRisksPaginated(
			@org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable)
			throws ResourceNotFoundException {

		GeneralResponse<Page<List<CustomResponse>>> response = new GeneralResponse<>();

		Page<List<CustomResponse>> risks = riskService.getAllRisks(pageable);
		response.setData(risks);
		response.setMessage("Risks fetched with pagination.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}
	
	@GetMapping("/all-risk-dropdown")
	public GeneralResponse<List<AllRiskDropdownResponse>> getAllRiskDropdown() throws ResourceNotFoundException{
		
		GeneralResponse<List<AllRiskDropdownResponse>> response = new GeneralResponse<>();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

			List<AllRiskDropdownResponse> risks = riskService.getAllRiskDropdown(pageable);
			response.setData(risks);
			response.setMessage("Risks fetched.");
			response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	
	@GetMapping("/all-assessments")
	public GeneralResponse<List<List<CustomResponse>>> getAllAssessments(){
		
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();
		try {
		    Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

			List<List<CustomResponse>> risks = riskService.getAllAssessment(pageable);
			response.setData(risks);
			response.setMessage("Risks fetched.");
			response.setStatus(ResponseStatus.SUCCESS);
		}catch(Exception e) {
			response.setMessage("Failed to delete.");
			response.setStatus(ResponseStatus.FAILED);
			System.out.println(e.getMessage());
		}
		
		return response;
	}
	
	@GetMapping("/get-risk-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskView(@PathVariable("id") Long id) throws ResourceNotFoundException{ 
		
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<List<CustomResponse>>();
		List<CustomResponse> data = riskService.getRiskView(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@GetMapping("/get-risk-assessment-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskAssessmentView(@PathVariable("id") Long id) throws ResourceNotFoundException{ 
		
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
