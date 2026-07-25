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
import ermorg.erm.dto.response.ApprovalDashboardResponse;
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

	@PostMapping("/{id}/trigger")
	public GeneralResponse<ApprovalResponse> trigger(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<ApprovalResponse> response = new GeneralResponse<>();
		response.setData(approvalService.trigger(id));
		response.setMessage("Approval workflow triggered.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@PostMapping("/{id}/escalate")
	public GeneralResponse<ApprovalResponse> escalate(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<ApprovalResponse> response = new GeneralResponse<>();
		response.setData(approvalService.escalate(id));
		response.setMessage("Approval escalated.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@PostMapping("/{id}/reminder")
	public GeneralResponse<ApprovalResponse> reminder(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<ApprovalResponse> response = new GeneralResponse<>();
		response.setData(approvalService.sendReminder(id));
		response.setMessage("Approval reminder sent.");
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

	@GetMapping("/dashboard")
	public GeneralResponse<ApprovalDashboardResponse> dashboard() throws ResourceNotFoundException {
		GeneralResponse<ApprovalDashboardResponse> response = new GeneralResponse<>();
		response.setData(approvalService.getMyDashboard());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/dashboard/upcoming-due")
	public GeneralResponse<List<ApprovalResponse>> upcomingDue() throws ResourceNotFoundException {
		GeneralResponse<List<ApprovalResponse>> response = new GeneralResponse<>();
		response.setData(approvalService.getMyUpcomingDueApprovals());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/dashboard/overdue")
	public GeneralResponse<List<ApprovalResponse>> overdue() throws ResourceNotFoundException {
		GeneralResponse<List<ApprovalResponse>> response = new GeneralResponse<>();
		response.setData(approvalService.getMyOverdueApprovals());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/dashboard/history")
	public GeneralResponse<List<ApprovalResponse>> history() throws ResourceNotFoundException {
		GeneralResponse<List<ApprovalResponse>> response = new GeneralResponse<>();
		response.setData(approvalService.getMyApprovalHistory());
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
