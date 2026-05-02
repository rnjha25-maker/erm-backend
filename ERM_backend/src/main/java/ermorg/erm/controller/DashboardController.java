package ermorg.erm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.BasicDashboardResponse;
import ermorg.erm.dto.response.CompanyAdminDashboardDto;
import ermorg.erm.dto.response.ErmDashboardSummaryResponse;
import ermorg.erm.dto.response.OrgAdminDashboardDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IDashboardService;

@RestController
public class DashboardController {

	@Autowired
	private IDashboardService dashboardService;
	
	@GetMapping("/basic/{period}")
	public GeneralResponse<BasicDashboardResponse>  getBasicDashboardData(@PathVariable String period, @PageableDefault(size = 20) Pageable pageable) throws ResourceNotFoundException {
		
		GeneralResponse<BasicDashboardResponse> response = new GeneralResponse<>();
		
		BasicDashboardResponse data = dashboardService.getBasicDashboardData(period, pageable);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/advanced/{period}")
	public GeneralResponse<CompanyAdminDashboardDto> getAdvancedDashboardData(@PathVariable String period, @PageableDefault(size = 20) Pageable pageable) throws ResourceNotFoundException {
		GeneralResponse<CompanyAdminDashboardDto> response = new GeneralResponse<>();
		
		CompanyAdminDashboardDto data = dashboardService.getCompanyAdminDashboardData(period, pageable);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/admin/{period}")
	public GeneralResponse<OrgAdminDashboardDto> getadminDashboardData(@PathVariable String period, @PageableDefault(size = 20) Pageable pageable) throws ResourceNotFoundException {
		
		GeneralResponse<OrgAdminDashboardDto> response = new GeneralResponse<>();
		
		OrgAdminDashboardDto data = dashboardService.getAdminDashboardData(period, pageable);
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/dashboard/erm-summary")
	public GeneralResponse<ErmDashboardSummaryResponse> getErmDashboardSummary(@RequestParam int year,
			@RequestParam String periodType, @RequestParam(required = false) Long companyId,
			@RequestParam(required = false) Long branchId, @RequestParam(required = false) Long functionId)
			throws ResourceNotFoundException {

		ErmDashboardPeriodType type;
		try {
			type = ErmDashboardPeriodType.valueOf(periodType.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid periodType. Use one of: Q1, Q2, Q3, Q4, H1, H2, YEAR");
		}

		GeneralResponse<ErmDashboardSummaryResponse> response = new GeneralResponse<>();
		ErmDashboardSummaryResponse data = dashboardService.getErmDashboardSummary(year, type, companyId, branchId,
				functionId);
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
}
