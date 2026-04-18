package ermorg.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.responseDTO.DashboardDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IDashboardService;

@RestController
@RequestMapping("/dashboard")
//@CrossOrigin
public class DashboardController {
	
	@Autowired
	private IDashboardService dashboardService;
	
	@GetMapping("/get-dashboard-data/{period}")
	public GeneralResponse<DashboardDto> getDashboardData(@PathVariable String period) throws ResourceNotFoundException {
		
		GeneralResponse<DashboardDto> response = new GeneralResponse<>();
		
		DashboardDto data = dashboardService.getDashboardData(period);
		
		response.setData(data);
		response.setMessage("Dashboard data fetched.");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}

}
