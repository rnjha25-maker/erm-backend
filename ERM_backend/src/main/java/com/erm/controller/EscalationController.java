package com.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.erm.dto.ResponseStatus;
import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.EscalationResponse;
import com.erm.dto.riskDTO.EscalationRequestDto;
import com.erm.exception.ResourceNotFoundException;
import com.erm.response.GeneralResponse;
import com.erm.service.IEscalationService;

@RestController
@RequestMapping("/esclation")
public class EscalationController {

	@Autowired
	private IEscalationService escalationServie;
	@PostMapping("/save")
	public GeneralResponse<EscalationResponse> save(@RequestBody EscalationRequestDto request) throws ResourceNotFoundException {
		
		GeneralResponse<EscalationResponse> response = new GeneralResponse<>();
		
		EscalationResponse data = escalationServie.save(request);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		
		return response;
	}
	
	@GetMapping("/{id}")
	public GeneralResponse<EscalationResponse> get(@PathVariable("id") Long esclationId) throws ResourceNotFoundException {
	
		GeneralResponse<EscalationResponse> response = new GeneralResponse<>();

		EscalationResponse data = escalationServie.get(esclationId);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	
	}
	
	@GetMapping("/get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getView(@PathVariable("id") Long esclationId) throws ResourceNotFoundException {
	
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();

		List<CustomResponse> data = escalationServie.getById(esclationId);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	
	}
	
	@GetMapping("/get-all")
	public GeneralResponse<List<List<CustomResponse>>> getEscalationList()  throws ResourceNotFoundException {
	
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();

		List<List<CustomResponse>> data = escalationServie.getAll();
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	
	}
	
	@DeleteMapping("/delete/{id}")
	public GeneralResponse<List<CustomResponse>> delete(@PathVariable("id") Long esclationId) throws ResourceNotFoundException {
	
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();

		escalationServie.delete(esclationId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Deleted.");
		return response;
	
	}
	
}
