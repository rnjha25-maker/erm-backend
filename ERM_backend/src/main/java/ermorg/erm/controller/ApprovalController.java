package ermorg.erm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.ApprovalLoginTargetResponse;
import ermorg.erm.dto.response.ApprovalResponse;
import ermorg.erm.dto.riskDTO.ApprovalDecisionRequest;
import ermorg.erm.dto.riskDTO.ApprovalRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IApprovalService;

@RestController
@RequestMapping("approvals")
public class ApprovalController {

	private final IApprovalService approvalService;

	public ApprovalController(IApprovalService approvalService) {
		this.approvalService = approvalService;
	}

	@PostMapping
	public GeneralResponse<ApprovalResponse> createApproval(@RequestBody ApprovalRequest request)
			throws ResourceNotFoundException {
		GeneralResponse<ApprovalResponse> response = new GeneralResponse<>();
		response.setData(approvalService.createApproval(request));
		response.setMessage("Approval task created.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@PutMapping("/{id}/decision")
	public GeneralResponse<ApprovalResponse> decide(@PathVariable Long id,
			@RequestBody ApprovalDecisionRequest request) throws ResourceNotFoundException {
		GeneralResponse<ApprovalResponse> response = new GeneralResponse<>();
		response.setData(approvalService.decide(id, request));
		response.setMessage("Approval decision saved.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/pending")
	public GeneralResponse<List<ApprovalResponse>> pending() throws ResourceNotFoundException {
		GeneralResponse<List<ApprovalResponse>> response = new GeneralResponse<>();
		response.setData(approvalService.getMyPendingApprovals());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/login-target")
	public GeneralResponse<ApprovalLoginTargetResponse> loginTarget() throws ResourceNotFoundException {
		GeneralResponse<ApprovalLoginTargetResponse> response = new GeneralResponse<>();
		response.setData(approvalService.getLoginTarget());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
}
