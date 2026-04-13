package com.erm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.erm.dto.ResponseStatus;
import com.erm.dto.response.BasicDashboardResponse;
import com.erm.dto.response.CompanyAdminDashboardDto;
import com.erm.dto.response.OrgAdminDashboardDto;
import com.erm.exception.ResourceNotFoundException;
import com.erm.response.GeneralResponse;
import com.erm.service.IDashboardService;

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
}
