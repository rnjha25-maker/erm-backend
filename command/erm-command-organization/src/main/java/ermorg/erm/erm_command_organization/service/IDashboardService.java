package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.responseDTO.DashboardDto;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IDashboardService {

	DashboardDto getDashboardData(String period) throws ResourceNotFoundException;

}
