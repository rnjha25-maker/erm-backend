package com.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.erm_command_organization.dto.requestDTO.BranchRequest;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponse;
import com.erm.erm_command_organization.dto.responseDTO.BranchResponseDto;
import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.service.IBranchService;

@RestController
@RequestMapping("branch")
//@CrossOrigin
public class BranchController {
	@Autowired
	private IBranchService branchService;

	@PostMapping("/create")
	public GeneralResponse<BranchResponse> createBranch(@RequestBody BranchRequest request)
			throws ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();
		BranchResponse branch = branchService.createBranch(request);
		response.setData(branch);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch created!");
		return response;
	}

	@PutMapping("/update")
	public GeneralResponse<BranchResponse> updateBranch(@RequestBody BranchRequest request)
			throws ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();

		BranchResponse branch = branchService.updateBranch(request);
		response.setData(branch);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch updated!");

		return response;
	}

	@DeleteMapping("/{id:[\\d]+}")
	public GeneralResponse<Void> deleteBranch(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();

		branchService.deleteBranch(id);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch deleted!");

		return response;
	}

	@GetMapping("/{branchId:[\\d]+}")
	public GeneralResponse<BranchResponseDto> getBranch(@PathVariable Long branchId) throws ResourceNotFoundException {
		GeneralResponse<BranchResponseDto> response = new GeneralResponse<>();

		BranchResponseDto branch = branchService.getBranch(branchId);
		
		response.setData(branch);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch deleted!");

		return response;
	}
	
	@GetMapping("/get-all-branches-by-company-id/{companyId:[\\d]+}")
	public GeneralResponse<BranchResponse> getAllBranchesByCompay(@PathVariable Long companyId) throws ResourceNotFoundException {
		GeneralResponse<BranchResponse> response = new GeneralResponse<>();

		BranchResponse branch = branchService.getAllBranchesByCompay(companyId);
		
		response.setData(branch);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Branch deleted!");

		return response;
	}
}
