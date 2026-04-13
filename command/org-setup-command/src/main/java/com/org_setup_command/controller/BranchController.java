package com.org_setup_command.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org_setup_command.dto.request.BranchDTO;
import com.org_setup_command.dto.response.BranchResponse;
import com.org_setup_command.dto.response.GeneralResponse;
import com.org_setup_command.dto.response.ResponseStatus;
import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.service.IBranchService;
import com.org_setup_command.service.InvalidDataException;

@RestController
@RequestMapping("branch")
public class BranchController {
	@Autowired
	private IBranchService branchService;

	@PostMapping("/create")
	public GeneralResponse<BranchResponse> createBranch(@RequestBody BranchDTO request) throws ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();
		BranchResponse branch = branchService.createBranch(request);
		response.setData(branch);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch created!");
		return response;
	}

	@PutMapping("/update")
	public GeneralResponse<BranchResponse> updateBranch(@RequestBody BranchDTO request) throws InvalidDataException, ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();
		
			BranchResponse branch = branchService.updateBranch(request);
			response.setData(branch);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Branch updated!");
		
		return response;
	}

	@DeleteMapping("/{id:[\\d]+}")
	public GeneralResponse<Void> deleteBranch(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();
		
			branchService.deleteBranch(id);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Branch deleted!");
		
		return response;
	}
	
	@GetMapping("/{id:[\\d]+}")
	public GeneralResponse<BranchResponse> getBranch(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();
		
		BranchResponse branch = branchService.getBranch(id);
		
			response.setData(branch);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Branch deleted!");
		
		return response;
	}
	
	@GetMapping("/all/{id:[\\d]+}")
	public GeneralResponse<List<BranchResponse>> getAllBranches(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException {
		GeneralResponse<List<BranchResponse>> response = new GeneralResponse<>();
		
		List<BranchResponse> branch = branchService.getAllBranch(id);
		
			response.setData(branch);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Branch deleted!");
		
		return response;
	}

}
